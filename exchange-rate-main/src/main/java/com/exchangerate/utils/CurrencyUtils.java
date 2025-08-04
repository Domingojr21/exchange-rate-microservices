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
    
    /**
     * Valida si un código de moneda está soportado usando el enum.
     * 
     * @param currency código de moneda a validar
     * @return true si está soportada, false en caso contrario
     */
    public static boolean isValidCurrency(String currency) {
        return SupportedCurrency.isSupported(currency);
    }
    
    /**
     * Calcula el monto convertido aplicando una tasa de cambio.
     * 
     * @param amount monto original
     * @param rate tasa de cambio
     * @return monto convertido con 2 decimales
     */
    public static BigDecimal calculateConvertedAmount(BigDecimal amount, BigDecimal rate) {
        if (amount == null || rate == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula la tasa de cambio basada en montos originales y convertidos.
     * 
     * @param originalAmount monto original
     * @param convertedAmount monto convertido
     * @return tasa de cambio con 4 decimales
     */
    public static BigDecimal calculateRate(BigDecimal originalAmount, BigDecimal convertedAmount) {
        if (originalAmount == null || convertedAmount == null || originalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return convertedAmount.divide(originalAmount, 4, RoundingMode.HALF_UP);
    }
    
    /**
     * Formatea una cantidad monetaria a string con 2 decimales.
     * 
     * @param amount cantidad a formatear
     * @return string formateado o "0.00" si amount es null
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }
    
    /**
     * Verifica si dos monedas forman un par válido.
     * 
     * @param sourceCurrency moneda de origen
     * @param targetCurrency moneda de destino
     * @return true si ambas son válidas y diferentes
     */
    public static boolean isValidCurrencyPair(String sourceCurrency, String targetCurrency) {
        return isValidCurrency(sourceCurrency) && 
               isValidCurrency(targetCurrency) && 
               !sourceCurrency.equalsIgnoreCase(targetCurrency);
    }
    
    /**
     * Obtiene el símbolo de una moneda.
     * 
     * @param currencyCode código de moneda
     * @return símbolo de la moneda o código si no se encuentra
     */
    public static String getCurrencySymbol(String currencyCode) {
        try {
            return SupportedCurrency.fromCode(currencyCode).getSymbol();
        } catch (IllegalArgumentException e) {
            return currencyCode;
        }
    }
}
