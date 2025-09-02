package org.vt.config.util;

public class CacheMakerException extends RuntimeException{
    public CacheMakerException(String message) {
        super(message);
    }

    public CacheMakerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheMakerException(Throwable cause) {
        super(cause);
    }
}
