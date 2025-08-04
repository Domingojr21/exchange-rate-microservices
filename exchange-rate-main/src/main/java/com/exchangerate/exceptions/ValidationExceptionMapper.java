package com.exchangerate.exceptions;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.exchangerate.models.response.ApiResponseConstants;
import com.exchangerate.models.response.ApiResponseWrapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Manejador global para excepciones de validación.
 * Estandariza las respuestas de error de validación utilizando el wrapper.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    
    private static final Logger LOG = Logger.getLogger(ValidationExceptionMapper.class);
    
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        LOG.warn("Violación de restricciones de validación", exception);
        
        List<String> errorMessages = exception.getConstraintViolations()
            .stream()
            .map(this::buildValidationErrorMessage)
            .collect(Collectors.toList());
        
        String combinedMessage = String.join(". ", errorMessages);
        
        return Response.status(ApiResponseConstants.HTTP_BAD_REQUEST)
            .entity(ApiResponseWrapper.badRequest(combinedMessage))
            .build();
    }
    
    private String buildValidationErrorMessage(ConstraintViolation<?> violation) {
        String property = extractPropertyName(violation.getPropertyPath().toString());
        return property + ": " + violation.getMessage();
    }
    
    private String extractPropertyName(String propertyPath) {
        int lastDotIndex = propertyPath.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < propertyPath.length() - 1) {
            return propertyPath.substring(lastDotIndex + 1);
        }
        return propertyPath;
    }
}