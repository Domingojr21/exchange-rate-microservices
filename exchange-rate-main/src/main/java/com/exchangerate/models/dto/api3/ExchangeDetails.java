package com.exchangerate.models.dto.api3;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;

/**
 * Detalles del intercambio de monedas para solicitudes avanzadas.
 * Encapsula la información específica de la transacción de cambio
 * requerida por proveedores financieros especializados.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterForReflection
public record ExchangeDetails(
    @JsonProperty("sourceCurrency")
    String sourceCurrency,
    
    @JsonProperty("targetCurrency")
    String targetCurrency,
    
    @JsonProperty("quantity")
    BigDecimal quantity
) implements Serializable {}