package org.vt.service.impl;

import com.voltage.securedata.enterprise.FPE;
import com.voltage.securedata.enterprise.LibraryContext;
import com.voltage.securedata.enterprise.VeException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.vt.LRUCacheComponent;
import org.vt.config.VoltageProperties;
import org.vt.config.mybatis.MyBatisUtils;
import org.vt.config.mybatis.entity.SdaConfig;
import org.vt.config.mybatis.entity.User;
import org.vt.config.mybatis.mapper.SdaConfigMapper;
import org.vt.config.util.CacheMakerException;
import org.vt.service.CacheMakerService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class CacheMakerServiceImpl implements CacheMakerService {

    private final Logger logger = LoggerFactory.getLogger("Cache Maker Service");

    private final VoltageProperties voltageProperties;

    public CacheMakerServiceImpl(VoltageProperties voltageProperties) {
        this.voltageProperties = voltageProperties;
    }

    @Override
    public void getCacheZip(Authentication authentication, HttpServletResponse response) {
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            User user = LRUCacheComponent.getInstance().getUserByUsername(authentication.getName());

            SdaConfigMapper sdaConfigMapper = myBatisUtils.createMapper(SdaConfigMapper.class, session);
            SdaConfig sdaConfig = sdaConfigMapper.getSdaConfigByConfidId(user.getConfigId());

            //branch.<region>.<code>@transit.file
            String wilayah = sdaConfig.getRegion();
            String identity = "branch." + sdaConfig.getRegion() + "." + sdaConfig.getCode() + "@transit.file";
            String cachePath = getCachePath(identity, wilayah);
            String txtPath = getUserKeyTxt(sdaConfig.getRegion(),sdaConfig.getCode(),wilayah);

            getZipFile(response, cachePath, txtPath, wilayah);
        } catch (Exception e) {
            throw new CacheMakerException(e);
        }
    }

    @Override
    public void getFpeProcessorZip(HttpServletResponse response) {
        Path zipPath = Paths.get(voltageProperties.getBasePath(), "installer.zip");

        // Validate file exists and is readable
        if (!Files.exists(zipPath) || !Files.isReadable(zipPath)) {
            logger.error("Zip file not found or not readable: {}", zipPath);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"installer.zip\"");
        response.setHeader("Content-Length", String.valueOf(zipPath.toFile().length()));

        try (ServletOutputStream out = response.getOutputStream()) {
            Files.copy(zipPath, out);
            out.flush();
        } catch (IOException ex) {
            logger.error("Failed to stream zip to response", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void getCacheZip(String identity, HttpServletResponse response) {
        String wilayah = extractWilayah(identity);
        String cachePath = getCachePath(identity, wilayah);
        Path zipPath = null;
        try (ServletOutputStream out = response.getOutputStream()) {
            if (cachePath == null || cachePath.isBlank()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate cache");
                return;
            }
            zipPath = Files.createTempFile("cache-", ".zip");
            zipDirectory(Paths.get(cachePath), zipPath);

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + wilayah + "-cache.zip");

            Files.copy(zipPath, out);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Cleanup
            deleteDirectoryRecursively(Paths.get(cachePath)); // cache folder
            if (zipPath != null) {
                try {
                    Files.deleteIfExists(zipPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getCachePath(String identity, String wilayah) {
        LibraryContext library = null;
        FPE fpe = null;

        String cachePath = voltageProperties.getBasePath() + "/" + wilayah;
        try {
            // Create the context for crypto operations
            library = new LibraryContext.Builder()
                    .setPolicyURL(voltageProperties.getPolicyUrl())
                    .setFileCachePath(cachePath)
                    .setTrustStorePath(voltageProperties.getTrustStorePath())
                    .setClientIdProduct(voltageProperties.getClientId(), voltageProperties.getClientIdVersion())
                    .build();

            // Protect and access the date
            fpe = library.getFPEBuilder(voltageProperties.getAlphanumericFormat())
                    .setSharedSecret(voltageProperties.getSharedSecret())
                    .setIdentity(identity) //branch.<wilayah>.<code>@transit.file branch.palembang.23HPL1178@transit.file
                    .build();

            fpe.protect("testCase");
            return cachePath;

        } catch (VeException ex) {
            logger.error("Failed: {}", ex.getDetailedMessage());
            return null;
        } finally {
            if (fpe != null) {
                fpe.delete();
            }
            // Explicit delete, required for JNI
            if (library != null) {
                library.delete();
            }
        }
    }

    private void deleteDirectoryRecursively(Path path) {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                logger.warn("Failed to delete path: {}", p, e);
                            }
                        });
            } catch (IOException ex) {
                logger.error("Failed to walk and delete directory: {}", path, ex);
            }
        }
    }

    // Utility method to zip a folder
    private void zipDirectory(Path sourceDirPath, Path zipPath) throws IOException {
        try (
                ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath));
                Stream<Path> paths = Files.walk(sourceDirPath)
        ) {
            paths
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            // Prefix all entries with "cache-dir/"
                            String entryName = "key-cache/" + sourceDirPath.relativize(path).toString().replace("\\", "/");
                            ZipEntry zipEntry = new ZipEntry(entryName);
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }
    // Utility method to zip a single file into an existing zip
    private void zipSingleFile(Path sourceFilePath, Path zipPath) throws IOException {
        // Create a temporary file to hold the updated zip
        Path tempZip = Files.createTempFile("updated-", ".zip");

        // Copy existing entries from original zip to temp
        try (
                ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempZip));
                ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))
        ) {
            // Copy existing zip entries
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                zos.putNextEntry(new ZipEntry(entry.getName()));
                zis.transferTo(zos);
                zos.closeEntry();
                zis.closeEntry();
            }

            // ✅ Add the new single file (e.g., user-info.txt)
            if (Files.exists(sourceFilePath)) {
                ZipEntry newEntry = new ZipEntry(sourceFilePath.getFileName().toString());
                zos.putNextEntry(newEntry);
                Files.copy(sourceFilePath, zos);
                zos.closeEntry();
            }
        }

        // Replace the original zip file with the updated one
        Files.move(tempZip, zipPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private String extractWilayah(String identity) {
        Pattern pattern = Pattern.compile("^branch\\.([^.]+)\\.[^@]+@transit\\.file$");
        Matcher matcher = pattern.matcher(identity);
        if (matcher.matches()) {
            return matcher.group(1); // wilayah
        } else {
            throw new IllegalArgumentException("Invalid identity format: " + identity);
        }
    }

    private void getZipFile(HttpServletResponse response, String cachePath, String txtPath, String wilayah) {
        Path zipPath = null;
        try (ServletOutputStream out = response.getOutputStream()) {
            if (cachePath == null || cachePath.isBlank()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate cache");
                return;
            }
            if (txtPath == null || txtPath.isBlank()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate user-key");
                return;
            }
            zipPath = Files.createTempFile("cache-", ".zip");
            zipDirectory(Paths.get(cachePath), zipPath);
            zipSingleFile(Paths.get(txtPath),zipPath);

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + wilayah + "-cache.zip");

            Files.copy(zipPath, out);
            out.flush();
        } catch (IOException ex) {
            logger.error("Failed to stream zip to response", ex);
        } finally {
            try {
                if (cachePath != null) {
                    deleteDirectoryRecursively(Paths.get(cachePath));
                }
                if (txtPath != null) {
                    Files.deleteIfExists(Paths.get(txtPath)); // just a file, not a directory
                }
                if (zipPath != null) {
                    Files.deleteIfExists(zipPath);
                }
            } catch (IOException e) {
                logger.warn("Cleanup failed", e);
            }
        }
    }

    private String getUserKeyTxt(String region,String code, String wilayah) throws IOException {
        String basePath = voltageProperties.getBasePath() + "/" + wilayah;
        String txtFilePath = basePath + "/user-key.txt";

        File txtFile = new File(txtFilePath);
        txtFile.getParentFile().mkdirs(); // Ensure directory exists

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            writer.newLine();
            writer.write(region + "." + code);
            writer.newLine();
        }
        return txtFilePath;
    }
}
