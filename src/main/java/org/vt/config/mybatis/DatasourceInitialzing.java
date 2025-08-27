package org.vt.config.mybatis;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DatasourceInitialzing implements InitializingBean {

    private static Logger logger = LogManager.getLogger();
    @Autowired
    DatasourceProperties datasourceProperties;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Environment environment;

    @Override
    public void afterPropertiesSet() {
        logger.info("================================================");
        System.out.println("==========================================");
        logger.info("Datasource   : {}", datasourceProperties.getDatasource());
        List<String> dsName = new ArrayList<>(datasourceProperties.getDatasource().keySet());
        dsName.parallelStream().forEach(key -> {
            try {
                Map<String, String> dsConf = datasourceProperties.getDatasource().get(key);
                boolean dsEnabled = true;
                if (dsConf.containsKey("Enabled"))
                    dsEnabled = Boolean.valueOf(dsConf.get("Enabled"));

                if (Datasource.dsCache.getIfPresent(key) == null) {
                    if (dsEnabled)
                        Datasource.addDatasource(key, dsConf, applicationContext);
                } else {
                    if (dsEnabled) {
                        HikariDataSource dataSource = Datasource.getDatasource(key);
                        Datasource.updateDatasource(key, dataSource, dsConf);
                    } else {
                        disableDatasource(key);
                    }
                }
            } catch (Exception e) {
                logger.warn(e, e);
            }
        });
    }

    private void disableDatasource(String key) throws NoSuchFieldException {
        logger.info("Stopping Datasource {}", key);
        HikariDataSource ds = Datasource.getDatasource(key);
        HikariPoolMXBean poolBean = ds.getHikariPoolMXBean();
        while (poolBean.getActiveConnections() > 0) {
            poolBean.softEvictConnections();
        }
        ds.close();
        Datasource.dsCache.invalidate(key);
    }
}
