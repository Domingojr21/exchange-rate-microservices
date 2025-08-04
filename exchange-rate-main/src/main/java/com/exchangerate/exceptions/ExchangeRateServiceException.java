package com.exchangerate.exceptions;

/**
 * Excepción general del servicio de tipo de cambio.
 * Se utiliza para representar fallos internos o errores inesperados
 * dentro de la lógica de negocio del servicio de tasas de cambio.
 *
 * A diferencia de {@link ApiProviderException}, esta excepción
 * no se limita a un proveedor específico, sino que refleja problemas
 * en la capa de servicio en general.
 * 
 * @author Dev. Domingo J. Ruiz
 */
public class ExchangeRateServiceException extends RuntimeException {
    
    /**
     * Construye una nueva excepción con un mensaje descriptivo.
     *
     * @param message mensaje detallado de la causa del error
     */
    public ExchangeRateServiceException(String message) {
        super(message);
    }
    
    /**
     * Construye una nueva excepción con un mensaje descriptivo
     * y la causa original del error.
     *
     * @param message mensaje detallado de la causa del error
     * @param cause   excepción original que causó este error
     */
    public ExchangeRateServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
