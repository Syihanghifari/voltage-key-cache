package org.vt.config.mybatis;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Map;

public class Datasource {
    private static final String USERNAME = "Username";
    private static final String PW_KEY = "Password";
    private static final String POOL_NAME = "PoolName";
    private static final String AUTO_COMMIT = "AutoCommit";
    private static final String MAX_POOL_SIZE = "MaximumPoolSize";
    private static final String MIN_IDLE = "MinimumIdle";
    private static final String DRIVER_CLAS = "DriverClass";
    private static final String JDBC_URL = "JdbcUrl";
    private static final String TEST_QUERY = "TestQuery";
    private static final String CONNECTION_TIMEOUT = "ConnectionTimeout";
    private static final String IDLE_TIMEOUT = "IdleTimeout";
    private static final String MAX_LIFETIME = "MaxLifetime";
    private static final String VALIDATION_TIMEOUT = "ValidationTimeout";
    static Cache<String, HikariDataSource> dsCache = Caffeine.newBuilder().build();
    private static Logger logger = LogManager.getLogger();

    private Datasource() {
        // Do Nothing
    }

    static void addDatasource(String dsName, Map<String, String> dsConf, ApplicationContext applicationContext) {
        HikariConfig config = new HikariConfig();
        if (!dsConf.containsKey(USERNAME) || !dsConf.containsKey(PW_KEY)) {
            logger.warn("Datasource {} credential not found, check Credhub", dsName);
        }

        String poolName = dsConf.containsKey(POOL_NAME) ? dsConf.get(POOL_NAME) : dsName;
        boolean autoCommit = dsConf.containsKey(AUTO_COMMIT) ? Boolean.valueOf(dsConf.get(AUTO_COMMIT)) : true;
        Integer maximumPoolSize = dsConf.containsKey(MAX_POOL_SIZE) ? Integer.valueOf(dsConf.get(MAX_POOL_SIZE)) : 1000;
        Integer minimumIdle = dsConf.containsKey(MIN_IDLE) ? Integer.valueOf(dsConf.get(MIN_IDLE)) : 1;
        String driverClassName = dsConf.containsKey(DRIVER_CLAS) ? dsConf.get(DRIVER_CLAS) : "";
        String jdbcUrl = dsConf.containsKey(JDBC_URL) ? dsConf.get(JDBC_URL) : "";
        String username = dsConf.containsKey(USERNAME) ? dsConf.get(USERNAME) : "";
        String password = dsConf.containsKey(PW_KEY) ? dsConf.get(PW_KEY) : "";
        String testQuery = dsConf.containsKey(TEST_QUERY) ? dsConf.get(TEST_QUERY) : "";
        Integer connectionTimeout = dsConf.containsKey(CONNECTION_TIMEOUT) ? Integer.valueOf(dsConf.get(CONNECTION_TIMEOUT)) : 2000;
        Integer idleTimeout = dsConf.containsKey(IDLE_TIMEOUT) ? Integer.valueOf(dsConf.get(IDLE_TIMEOUT)) : 900000;
        Integer maxLifetime = dsConf.containsKey(MAX_LIFETIME) ? Integer.valueOf(dsConf.get(MAX_LIFETIME)) : 1800000;
        Integer validationTimeOut = dsConf.containsKey(VALIDATION_TIMEOUT) ? Integer.valueOf(dsConf.get(VALIDATION_TIMEOUT)) : 3000;

        try {
            config.setAutoCommit(autoCommit);
            config.setMaximumPoolSize(maximumPoolSize);
            config.setMinimumIdle(minimumIdle);
            config.setPoolName(poolName);
            config.setDriverClassName(driverClassName);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setConnectionTestQuery(testQuery);
            config.setInitializationFailTimeout(connectionTimeout);

            config.setConnectionTimeout(connectionTimeout);
            config.setValidationTimeout(validationTimeOut);
            config.setIdleTimeout(idleTimeout);
            config.setMaxLifetime(maxLifetime);
            config.setAllowPoolSuspension(true); // allow suspension through JMX
            config.setRegisterMbeans(true);
            HikariDataSource dataSource = new HikariDataSource(config);
            if (!applicationContext.containsBean(poolName)) {
                ((GenericApplicationContext) applicationContext).registerBean(poolName, HikariDataSource.class, dataSource);
            }
            dsCache.put(dsName, dataSource);
        } catch (Exception ex) {
            logger.warn("Add Datasource {} failed, reason: {}", poolName, ex.getMessage());
        }
    }

    static void updateDatasource(String dsName, HikariDataSource hikariDS, Map<String, String> dsConf) {
        String poolName = dsConf.containsKey(POOL_NAME) ? dsConf.get(POOL_NAME) : dsName;
        Integer maximumPoolSize = dsConf.containsKey(MAX_POOL_SIZE) ? Integer.valueOf(dsConf.get(MAX_POOL_SIZE)) : 1000;
        Integer minimumIdle = dsConf.containsKey(MIN_IDLE) ? Integer.valueOf(dsConf.get(MIN_IDLE)) : 1;
        String username = dsConf.containsKey(USERNAME) ? dsConf.get(USERNAME) : "";
        String password = dsConf.containsKey(PW_KEY) ? dsConf.get(PW_KEY) : "";
        Integer connectionTimeout = dsConf.containsKey(CONNECTION_TIMEOUT) ? Integer.valueOf(dsConf.get(CONNECTION_TIMEOUT)) : 2000;
        Integer idleTimeout = dsConf.containsKey(IDLE_TIMEOUT) ? Integer.valueOf(dsConf.get(IDLE_TIMEOUT)) : 900000;
        Integer maxLifetime = dsConf.containsKey(MAX_LIFETIME) ? Integer.valueOf(dsConf.get(MAX_LIFETIME)) : 1800000;

        try {
            HikariConfigMXBean hikariConfigMXBean = hikariDS.getHikariConfigMXBean();
            if (hikariConfigMXBean.getMaximumPoolSize() != maximumPoolSize)
                hikariConfigMXBean.setMaximumPoolSize(maximumPoolSize);
            if (hikariConfigMXBean.getMinimumIdle() != minimumIdle)
                hikariConfigMXBean.setMinimumIdle(minimumIdle);
            hikariConfigMXBean.setUsername(username);
            hikariConfigMXBean.setPassword(password);
            if (hikariConfigMXBean.getConnectionTimeout() != connectionTimeout)
                hikariConfigMXBean.setConnectionTimeout(connectionTimeout);
            if (hikariConfigMXBean.getIdleTimeout() != idleTimeout)
                hikariConfigMXBean.setIdleTimeout(idleTimeout);
            if (hikariConfigMXBean.getMaxLifetime() != maxLifetime)
                hikariConfigMXBean.setMaxLifetime(maxLifetime);
        } catch (Exception ex) {
            logger.warn("Update Datasource {} failed, reason: {}", poolName, ex.getMessage());
        }
    }

    public static HikariDataSource getDatasource(String dsName) throws NoSuchFieldException {
        HikariDataSource result = null;

        if (dsCache.getIfPresent(dsName) != null) {
            result = dsCache.getIfPresent(dsName);
        } else {
            throw new NoSuchFieldException("Datasource " + dsName + " not found");
        }
        return result;
    }

    public static void killAllDatasouce() {
        dsCache.asMap().forEach((key, value) -> {
            try {
                logger.info("Stopping Datasource {}", key);
                HikariDataSource datasource = getDatasource(key);
                HikariPoolMXBean poolBean = datasource.getHikariPoolMXBean();
                while (poolBean.getActiveConnections() > 0) {
                    poolBean.softEvictConnections();
                }
                datasource.close();
                dsCache.invalidate(key);
            } catch (Exception e) {
                logger.warn("Stop Datasource Failed, Reason: {}", e.getMessage());
            }
        });
    }
}
