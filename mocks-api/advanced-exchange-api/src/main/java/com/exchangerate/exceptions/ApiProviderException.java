package com.exchangerate.exceptions;

public class ApiProviderException extends RuntimeException {
    
    private final String provider;
    
    public ApiProviderException(String provider, String message) {
        super(message);
        this.provider = provider;
    }
    
    public ApiProviderException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
    }
    
    public String getProvider() {
        return provider;
    }
}