package com.exchangerate.exceptions;

import org.jboss.logging.Logger;

import com.exchangerate.utils.ApiResponseConstants;
import com.exchangerate.models.response.ApiResponseWrapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Manejador global para excepciones no controladas.
 * Estandariza todas las respuestas de error utilizando el wrapper.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Throwable> {
    
    private static final Logger LOG = Logger.getLogger(GeneralExceptionMapper.class);
    
    @Override
    public Response toResponse(Throwable exception) {
        LOG.error("Error no controlado en la aplicación", exception);
        
        if (exception instanceof WebApplicationException webEx) {
            int statusCode = webEx.getResponse().getStatus();
            String message = webEx.getMessage();
            
            if (message == null || message.isEmpty()) {
                message = "Error en la aplicación";
            }
            
            return Response.status(statusCode)
                .entity(ApiResponseWrapper.error(statusCode, message))
                .build();
        }
        
        // Para excepciones no HTTP, usamos 500 Internal Server Error
        return Response.status(ApiResponseConstants.HTTP_SERVER_ERROR)
            .entity(ApiResponseWrapper.serverError(
                "Error interno del servidor. Por favor, contacte al administrador."))
            .build();
    }
}