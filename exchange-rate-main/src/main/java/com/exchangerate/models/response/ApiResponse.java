package com.exchangerate.models.response;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Respuesta unificada de los proveedores de API de tipo de cambio.
 * Encapsula el resultado de cada proveedor incluyendo métricas
 * de rendimiento y manejo de errores para el sistema de comparación.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterForReflection
public record ApiResponse(
    String provider,
    BigDecimal rate,
    BigDecimal convertedAmount,
    Long responseTimeMs,
    boolean successful,
    String errorMessage
) {
     /**
     * Crea una respuesta exitosa de proveedor de API.
     * 
     * @param provider nombre del proveedor de API
     * @param rate tasa de cambio calculada
     * @param convertedAmount monto convertido final
     * @param responseTimeMs tiempo de respuesta en milisegundos
     * @return instancia de ApiResponse para resultado exitoso
     */
    public static ApiResponse success(String provider, BigDecimal rate, BigDecimal convertedAmount, Long responseTimeMs) {
        return new ApiResponse(provider, rate, convertedAmount, responseTimeMs, true, null);
    }
    
     /**
     * Crea una respuesta de error de proveedor de API.
     * 
     * @param provider nombre del proveedor de API
     * @param errorMessage mensaje descriptivo del error
     * @param responseTimeMs tiempo de respuesta en milisegundos
     * @return instancia de ApiResponse para resultado fallido
     */
    public static ApiResponse failure(String provider, String errorMessage, Long responseTimeMs) {
        return new ApiResponse(provider, null, null, responseTimeMs, false, errorMessage);
    }
}