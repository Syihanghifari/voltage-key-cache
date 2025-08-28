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
import org.vt.config.VoltageProperties;
import org.vt.config.mybatis.MyBatisUtils;
import org.vt.config.mybatis.entity.SdaConfig;
import org.vt.config.mybatis.entity.User;
import org.vt.config.mybatis.mapper.SdaConfigMapper;
import org.vt.config.mybatis.mapper.UserMapper;
import org.vt.service.CacheMakerService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CacheMakerServiceImpl implements CacheMakerService {

    private final Logger logger = LoggerFactory.getLogger("Voltage Backend Service");

    private final VoltageProperties voltageProperties;

    public CacheMakerServiceImpl(VoltageProperties voltageProperties) {
        this.voltageProperties = voltageProperties;
    }

    @Override
    public void getCacheZip(Authentication authentication, HttpServletResponse response){
        MyBatisUtils myBatisUtils = new MyBatisUtils();
        SqlSessionFactory sqlSessionFactory = myBatisUtils.createFactory();
        try(SqlSession session = sqlSessionFactory.openSession(false)){
            UserMapper userMapper = myBatisUtils.createMapper(UserMapper.class, session);
            User user = userMapper.getUserByUsername(authentication.getName());

            SdaConfigMapper sdaConfigMapper = myBatisUtils.createMapper(SdaConfigMapper.class,session);
            SdaConfig sdaConfig = sdaConfigMapper.getSdaConfigByConfidId(user.getConfigId());

            //branch.<region>.<code>@transit.file
            String wilayah = sdaConfig.getRegion();
            String identity = "branch." + sdaConfig.getRegion()+"."+sdaConfig.getCode() + "@transit.file";
            String cachePath = getCachePath(identity,wilayah);

            getZipFile(response,cachePath,wilayah);
        } catch (Exception e) {
            logger.error("get data failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getCacheZip(String identity, HttpServletResponse response){
        String wilayah = extractWilayah(identity);
        String cachePath = getCachePath(identity,wilayah);
        Path zipPath = null;
        try(ServletOutputStream out = response.getOutputStream()){
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
        }catch (IOException ex){
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

    private String getCachePath(String identity,String wilayah){
        LibraryContext library = null;
        FPE fpe     = null;

        String cachePath = voltageProperties.getBasePath() + "/" +wilayah;

        logger.info("policyUrl is : " + voltageProperties.getPolicyUrl());
        logger.info("trustore path is : " + voltageProperties.getTrustStorePath());
        logger.info("format is : " + voltageProperties.getAlphanumericFormat());
        logger.info("sharedsecret is : " + voltageProperties.getSharedSecret());
        logger.info("cachepath is : " + cachePath);

        try{
            // Create the context for crypto operations
            library = new LibraryContext.Builder()
                    .setPolicyURL(voltageProperties.getPolicyUrl())
                    .setFileCachePath(cachePath)
                    .setTrustStorePath(voltageProperties.getTrustStorePath())
                    .setClientIdProduct(voltageProperties.getClientId(),voltageProperties.getClientIdVersion())
                    .build();

            // Protect and access the date
            fpe = library.getFPEBuilder(voltageProperties.getAlphanumericFormat())
                    .setSharedSecret(voltageProperties.getSharedSecret())
                    .setIdentity(identity) //branch.<wilayah>.<code>@transit.file branch.palembang.23HPL1178@transit.file
                    .build();

            fpe.protect("testCase");
            return cachePath;

        }catch (VeException ex) {
            System.out.println("Failed: " + ex.getDetailedMessage());
            return null;
        } catch (Throwable ex) {
            System.out.println("Failed: Unexpected exception" + ex);
            ex.printStackTrace();
            return null;
        }finally {
            if (fpe != null) {
                fpe.delete();
            }
            // Explicit delete, required for JNI
            if (library != null) {
                library.delete();
            }
        }
    }

    private void deleteDirectoryRecursively(Path path){
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder()) // Delete files before directories
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to delete: " + p, e);
                            }
                        });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    // Utility method to zip a folder
    private void zipDirectory(Path sourceDirPath, Path zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walk(sourceDirPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
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

    private void getZipFile(HttpServletResponse response ,String cachePath, String wilayah){
        Path zipPath = null;
        try(ServletOutputStream out = response.getOutputStream()){
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
        }catch (IOException ex){
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
}
