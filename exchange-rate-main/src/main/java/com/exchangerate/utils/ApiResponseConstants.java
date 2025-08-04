package com.exchangerate.utils;

/**
 * Constantes utilizadas en las respuestas de la API.
 * Incluye códigos HTTP y mensajes estandarizados.
 */
public final class ApiResponseConstants {
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_METHOD_NOT_ALLOWED = 405;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    public static final int HTTP_SERVER_ERROR = 500;
    public static final int HTTP_SERVICE_UNAVAILABLE = 503;
    
    public static final String SUCCESS_MESSAGE = "Operación exitosa";
    public static final String ERROR_MESSAGE = "Error en la operación";
    public static final String VALIDATION_ERROR = "Error de validación";
    public static final String NOT_FOUND_ERROR = "Recurso no encontrado";
    public static final String SERVER_ERROR = "Error interno del servidor";
    public static final String SERVICE_UNAVAILABLE = "Servicio temporalmente no disponible";
    
    public static final String INVALID_CURRENCY = "Moneda no válida";
    public static final String SAME_CURRENCY_ERROR = "No se puede convertir a la misma moneda";
    public static final String PROVIDER_NOT_AVAILABLE = "No hay proveedores de tipo de cambio disponibles";
    public static final String INVALID_AMOUNT = "El monto debe ser mayor a cero";
    
    public static final String NO_PROVIDER_AVAILABLE = "NO_PROVIDER_AVAILABLE";
    
    private ApiResponseConstants() {
        throw new AssertionError("No debe instanciar esta clase de utilidad");
    }
}