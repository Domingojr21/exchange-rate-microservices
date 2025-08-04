package com.exchangerate.models;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Solicitud para el proveedor de tipo de cambio con formato JSON simple.
 * Utiliza una estructura plana con campos directos para la conversión
 * de monedas en el API Provider 1.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterForReflection
public record SimpleExchangeRequest(
    @JsonProperty("from")
    String from,
    
    @JsonProperty("to") 
    String to,
    
    @JsonProperty("value")
    BigDecimal value
) implements Serializable {}