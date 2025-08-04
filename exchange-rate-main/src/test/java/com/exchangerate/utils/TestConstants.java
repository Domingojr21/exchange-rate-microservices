package com.exchangerate.utils;

import java.math.BigDecimal;

/**
 * Constantes utilizadas en las pruebas para mantener consistencia
 * y evitar duplicación de literales.
 */
public final class TestConstants {
    public static final String USD = "USD";
    public static final String EUR = "EUR";
    public static final String MXN = "MXN";
    public static final String DOP = "DOP";
    
    public static final BigDecimal AMOUNT_100 = new BigDecimal("100.00");
    public static final BigDecimal AMOUNT_50 = new BigDecimal("50.00");
    public static final BigDecimal AMOUNT_25 = new BigDecimal("25.00");
    
    public static final BigDecimal RATE_USD_EUR = new BigDecimal("0.85");
    public static final BigDecimal RATE_USD_MXN = new BigDecimal("17.50");
    public static final BigDecimal RATE_EUR_DOP = new BigDecimal("68.50");
    
    public static final BigDecimal CONVERTED_USD_EUR = new BigDecimal("85.00");
    public static final BigDecimal CONVERTED_USD_MXN = new BigDecimal("875.00");
    public static final BigDecimal CONVERTED_EUR_DOP = new BigDecimal("1712.50");
    
    public static final String SIMPLE_PROVIDER = "SIMPLE_JSON_PROVIDER";
    public static final String XML_PROVIDER = "XML_BANKING_PROVIDER";
    public static final String ADVANCED_PROVIDER = "ADVANCED_FINTECH_PROVIDER";
    public static final String NO_PROVIDER = "NO_PROVIDER_AVAILABLE";
    
    public static final long RESPONSE_TIME = 200L;
    
    public static final int SUCCESSFUL_PROVIDERS_ALL = 3;
    public static final int SUCCESSFUL_PROVIDERS_SOME = 2;
    public static final int SUCCESSFUL_PROVIDERS_NONE = 0;
    public static final int TOTAL_PROVIDERS = 3;
    
    private TestConstants() {
    }
}