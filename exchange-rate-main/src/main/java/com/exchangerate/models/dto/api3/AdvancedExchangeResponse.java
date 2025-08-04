package com.exchangerate.models.dto.api3;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;

/**
 * Respuesta del proveedor de tipo de cambio con formato JSON avanzado.
 * Incluye código de estado, mensaje descriptivo y datos de resultado
 * en estructura anidada del API Provider 3.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterForReflection
public record AdvancedExchangeResponse(
    @JsonProperty("statusCode")
    Integer statusCode,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("data")
    ExchangeResultData data
) implements Serializable {}