package com.exchangerate.models.request;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Solicitud para obtener el mejor tipo de cambio entre monedas.
 * Esta clase encapsula los datos necesarios para consultar múltiples
 * proveedores de APIs de tipo de cambio y obtener la mejor oferta.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterForReflection
public record ExchangeRateRequest(
    @JsonProperty("sourceCurrency")
    @NotBlank(message = "Source currency es requerido")
    String sourceCurrency,
    
    @JsonProperty("targetCurrency") 
    @NotBlank(message = "Target currency es requerido")
    String targetCurrency,
    
    @JsonProperty("amount")
    @NotNull(message = "Amount es requerido")
    @DecimalMin(value = "0.01", message = "Amount debe ser mayor a cero")
    BigDecimal amount
) implements Serializable {}