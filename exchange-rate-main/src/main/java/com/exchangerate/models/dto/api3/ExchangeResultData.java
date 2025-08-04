package com.exchangerate.models.dto.api3;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;

/**
 * Datos del resultado de conversión para respuestas avanzadas.
 * Contiene el total calculado de la operación de cambio de moneda
 * procesada por servicios financieros especializados.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterForReflection
public record ExchangeResultData(
    @JsonProperty("total")
    BigDecimal total
) implements Serializable {}