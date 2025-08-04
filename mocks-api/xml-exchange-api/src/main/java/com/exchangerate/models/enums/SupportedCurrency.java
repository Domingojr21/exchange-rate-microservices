package com.exchangerate.models.enums;

/**
 * Enumeración de monedas soportadas por el sistema.
 * Solo incluye las monedas más comunes para intercambio.
 * 
 * @author Dev. Domingo J. Ruiz
 */
public enum SupportedCurrency {
    USD("Dólar Estadounidense", "$"),
    EUR("Euro", "€"), 
    MXN("Peso Mexicano", "$"),
    DOP("Peso Dominicano", "RD$");
    
    private final String description;
    private final String symbol;
    
    SupportedCurrency(String description, String symbol) {
        this.description = description;
        this.symbol = symbol;
    }
    
    public String getDescription() { return description; }
    public String getSymbol() { return symbol; }
    
    public static boolean isSupported(String currencyCode) {
        if (currencyCode == null) return false;
        try {
            valueOf(currencyCode.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}