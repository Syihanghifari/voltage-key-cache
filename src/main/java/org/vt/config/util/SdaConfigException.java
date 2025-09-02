package org.vt.config.util;

public class SdaConfigException extends RuntimeException{
    public SdaConfigException(String message) {
        super(message);
    }

    public SdaConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public SdaConfigException(Throwable cause) {
        super(cause);
    }
}
