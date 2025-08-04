package com.exchangerate.models.request;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para obtener el mejor tipo de cambio entre monedas.
 * Esta clase encapsula los datos necesarios para consultar múltiples
 * proveedores de APIs de tipo de cambio y obtener la mejor oferta.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterForReflection
public record ExchangeRateRequest(
    @NotBlank(message = "La moneda de origen es obligatoria")
    @Size(min = 3, max = 3, message = "El código de moneda debe tener exactamente 3 caracteres")
    @JsonProperty("sourceCurrency") String sourceCurrency,
    
    @NotBlank(message = "La moneda de destino es obligatoria")
    @Size(min = 3, max = 3, message = "El código de moneda debe tener exactamente 3 caracteres")
    @JsonProperty("targetCurrency") String targetCurrency,
    
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @JsonProperty("amount") BigDecimal amount
) implements Serializable {
    /**
     * Valida que las monedas de origen y destino no sean iguales.
     * Este método es usado por el framework de validación de Jakarta.
     * 
     * @return true si las monedas son diferentes, false si son iguales
     */
    public boolean isValid() {
        if (sourceCurrency == null || targetCurrency == null) {
            return true; // Otra validación manejará los nulos
        }
        return !sourceCurrency.equalsIgnoreCase(targetCurrency);
    }

}