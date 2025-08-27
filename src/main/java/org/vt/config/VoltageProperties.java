package org.vt.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config.voltage")
public class VoltageProperties {
    private String policyUrl;
    private String trustStorePath;
    private String basePath;
    private String sharedSecret;
    private String alphanumericFormat;
    private String numericFormat;
    private String alphaFormat;
    private String clientId;
    private String clientIdVersion;

    public String getPolicyUrl() {
        return policyUrl;
    }

    public void setPolicyUrl(String policyUrl) {
        this.policyUrl = policyUrl;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String getAlphanumericFormat() {
        return alphanumericFormat;
    }

    public void setAlphanumericFormat(String alphanumericFormat) {
        this.alphanumericFormat = alphanumericFormat;
    }

    public String getNumericFormat() {
        return numericFormat;
    }

    public void setNumericFormat(String numericFormat) {
        this.numericFormat = numericFormat;
    }

    public String getAlphaFormat() {
        return alphaFormat;
    }

    public void setAlphaFormat(String alphaFormat) {
        this.alphaFormat = alphaFormat;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientIdVersion() {
        return clientIdVersion;
    }

    public void setClientIdVersion(String clientIdVersion) {
        this.clientIdVersion = clientIdVersion;
    }
}
