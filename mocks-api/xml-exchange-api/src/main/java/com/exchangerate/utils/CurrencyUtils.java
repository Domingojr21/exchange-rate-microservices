package com.exchangerate.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.exchangerate.models.enums.SupportedCurrency;

/**
 * Utilidades para manejo de monedas y cálculos de conversión.
 * Ahora usa el enum de monedas soportadas para validación.
 * 
 * @author Dev. Domingo J. Ruiz
 */
public class CurrencyUtils {
    
    public static boolean isValidCurrency(String currency) {
        return SupportedCurrency.isSupported(currency);
    }
    
    public static BigDecimal calculateConvertedAmount(BigDecimal amount, BigDecimal rate) {
        if (amount == null || rate == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
    
    public static boolean isValidCurrencyPair(String sourceCurrency, String targetCurrency) {
        return isValidCurrency(sourceCurrency) && 
               isValidCurrency(targetCurrency) && 
               !sourceCurrency.equalsIgnoreCase(targetCurrency);
    }
}