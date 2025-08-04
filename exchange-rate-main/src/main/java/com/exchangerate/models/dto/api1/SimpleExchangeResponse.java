package com.exchangerate.models.dto.api1;

import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Respuesta del proveedor de tipo de cambio con formato JSON simple.
 * Contiene únicamente la tasa de cambio calculada por el API Provider 1.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterForReflection
public record SimpleExchangeResponse(
    @JsonProperty("rate")
    BigDecimal rate
) implements Serializable {}