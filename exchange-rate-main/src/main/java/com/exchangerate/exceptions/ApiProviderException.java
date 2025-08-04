package com.exchangerate.exceptions;

/**
 * Excepción personalizada para manejar errores específicos de un proveedor de tipo de cambio.
 * Esta excepción se utiliza cuando ocurre un fallo en la comunicación,
 * validación o procesamiento de datos con un proveedor externo de tasas de cambio.
 *
 * Contiene información adicional sobre el nombre del proveedor que originó el error.
 * 
 * @author Dev. Domingo J. Ruiz
 */
public class ApiProviderException extends RuntimeException {
    
    private final String provider;
    
    /**
     * Construye una nueva excepción con el proveedor y un mensaje descriptivo.
     *
     * @param provider nombre del proveedor que generó el error
     * @param message  mensaje detallado de la causa del error
     */
    public ApiProviderException(String provider, String message) {
        super(message);
        this.provider = provider;
    }
    
    /**
     * Construye una nueva excepción con el proveedor, un mensaje descriptivo
     * y la causa original del error.
     *
     * @param provider nombre del proveedor que generó el error
     * @param message  mensaje detallado de la causa del error
     * @param cause    excepción original que causó este error
     */
    public ApiProviderException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
    }
    
    /**
     * Obtiene el nombre del proveedor que originó la excepción.
     *
     * @return nombre del proveedor
     */
    public String getProvider() {
        return provider;
    }
}
