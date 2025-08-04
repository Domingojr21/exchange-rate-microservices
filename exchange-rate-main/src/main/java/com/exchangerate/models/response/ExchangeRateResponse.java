package com.exchangerate.models.response;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Respuesta final del sistema de comparación de tipos de cambio.
 * Contiene el mejor tipo de cambio encontrado entre todos los proveedores
 * consultados, incluyendo métricas completas de la operación.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterForReflection
public record ExchangeRateResponse(
    
    @JsonProperty("bestRate")
    BigDecimal bestRate,
    
    @JsonProperty("convertedAmount")
    BigDecimal convertedAmount,
    
    @JsonProperty("provider")
    String provider,
    
    @JsonProperty("responseTimeMs")
    Long responseTimeMs,
    
    @JsonProperty("successfulProviders")
    Integer successfulProviders,
    
    @JsonProperty("totalProviders")
    Integer totalProviders
) implements Serializable {}