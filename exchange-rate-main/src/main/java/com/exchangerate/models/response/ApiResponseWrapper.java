package com.exchangerate.models.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import com.exchangerate.utils.ApiResponseConstants;


/**
 * Wrapper genérico para estandarizar todas las respuestas de la API.
 * Proporciona una estructura consistente independientemente del tipo de respuesta.
 *
 * @param <T> tipo de datos que contiene la respuesta
 */
@RegisterForReflection
public record ApiResponseWrapper<T>(
    @JsonProperty("code")
    Integer code,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("data")
    T data
) implements Serializable {

    /**
     * Crea una respuesta exitosa con datos.
     *
     * @param <T> tipo de datos
     * @param data datos de la respuesta
     * @return wrapper con código 200 y mensaje de éxito
     */
    public static <T> ApiResponseWrapper<T> success(T data) {
        return new ApiResponseWrapper<>(
            ApiResponseConstants.HTTP_OK, 
            ApiResponseConstants.SUCCESS_MESSAGE, 
            data
        );
    }
    
    /**
     * Crea una respuesta exitosa con mensaje personalizado.
     *
     * @param <T> tipo de datos
     * @param data datos de la respuesta
     * @param message mensaje personalizado
     * @return wrapper con código 200 y mensaje personalizado
     */
    public static <T> ApiResponseWrapper<T> success(T data, String message) {
        return new ApiResponseWrapper<>(
            ApiResponseConstants.HTTP_OK, 
            message, 
            data
        );
    }
    
    /**
     * Crea una respuesta de error.
     *
     * @param <T> tipo de datos
     * @param code código HTTP de error
     * @param message mensaje de error
     * @return wrapper con error sin datos
     */
    public static <T> ApiResponseWrapper<T> error(Integer code, String message) {
        return new ApiResponseWrapper<>(code, message, null);
    }
    
    /**
     * Crea una respuesta de error con datos.
     *
     * @param <T> tipo de datos
     * @param code código HTTP de error
     * @param message mensaje de error
     * @param data datos adicionales del error
     * @return wrapper con error y datos
     */
    public static <T> ApiResponseWrapper<T> error(Integer code, String message, T data) {
        return new ApiResponseWrapper<>(code, message, data);
    }
    
    /**
     * Crea una respuesta de error 400 (Bad Request).
     *
     * @param <T> tipo de datos
     * @param message mensaje de error
     * @return wrapper con error 400
     */
    public static <T> ApiResponseWrapper<T> badRequest(String message) {
        return error(ApiResponseConstants.HTTP_BAD_REQUEST, message);
    }
    
    /**
     * Crea una respuesta de error 404 (Not Found).
     *
     * @param <T> tipo de datos
     * @param message mensaje de error
     * @return wrapper con error 404
     */
    public static <T> ApiResponseWrapper<T> notFound(String message) {
        return error(ApiResponseConstants.HTTP_NOT_FOUND, message);
    }
    
    /**
     * Crea una respuesta de error 500 (Internal Server Error).
     *
     * @param <T> tipo de datos
     * @param message mensaje de error
     * @return wrapper con error 500
     */
    public static <T> ApiResponseWrapper<T> serverError(String message) {
        return error(ApiResponseConstants.HTTP_SERVER_ERROR, message);
    }
    
    /**
     * Crea una respuesta de error 503 (Service Unavailable).
     *
     * @param <T> tipo de datos
     * @param message mensaje de error
     * @return wrapper con error 503
     */
    public static <T> ApiResponseWrapper<T> serviceUnavailable(String message) {
        return error(ApiResponseConstants.HTTP_SERVICE_UNAVAILABLE, message);
    }
    
    /**
     * Crea una respuesta de error 503 (Service Unavailable) con datos.
     *
     * @param <T> tipo de datos
     * @param message mensaje de error
     * @param data datos adicionales
     * @return wrapper con error 503 y datos
     */
    public static <T> ApiResponseWrapper<T> serviceUnavailable(String message, T data) {
        return error(ApiResponseConstants.HTTP_SERVICE_UNAVAILABLE, message, data);
    }
}