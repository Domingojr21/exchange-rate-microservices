package com.exchangerate.models.response;

import java.io.Serializable;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Interfaz para definir el comportamiento básico esperado
 * de las respuestas API del sistema.
 *
 * @param <T> tipo de datos de la respuesta
 */
@RegisterForReflection
public interface GenericResponse<T> extends Serializable {
    
    /**
     * Obtiene los datos específicos de la respuesta.
     *
     * @return datos de la respuesta
     */
    T getData();
}