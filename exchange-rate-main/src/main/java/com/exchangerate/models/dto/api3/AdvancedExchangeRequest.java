package com.exchangerate.models.dto.api3;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;

/**
 * Solicitud para el proveedor de tipo de cambio con formato JSON avanzado.
 * Utiliza estructura anidada compleja para servicios financieros
 * especializados del API Provider 3.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterForReflection
public record AdvancedExchangeRequest(
    @JsonProperty("exchange")
    ExchangeDetails exchange
) implements Serializable {}