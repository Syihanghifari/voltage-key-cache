package org.vt.config.mybatis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "config", ignoreInvalidFields = true, ignoreUnknownFields = true)
public class DatasourceProperties {

    private Map<String, Map<String, String>> datasource;

    public Map<String, Map<String, String>> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<String, Map<String, String>> datasource) {
        this.datasource = datasource;
    }
}