package com.exchangerate.resources;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import java.math.BigDecimal;

import com.exchangerate.models.SimpleExchangeRequest;
import com.exchangerate.models.SimpleExchangeResponse;
import com.exchangerate.models.enums.SupportedCurrency;
import com.exchangerate.services.RandomRateGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.RolesAllowed;

/**
 * Recurso REST para Simple Exchange API.
 * Proporciona tasas de cambio en formato JSON simple.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@Path("/exchange")
@ApplicationScoped
@RolesAllowed("user")
public class SimpleExchangeResource {
    
    private static final Logger LOG = Logger.getLogger(SimpleExchangeResource.class);
    
    @Inject
    RandomRateGenerator rateGenerator;
    
    @ConfigProperty(name = "provider.response.delay.min")
    int delayMin;
    
    @ConfigProperty(name = "provider.response.delay.max")
    int delayMax;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExchangeRate(SimpleExchangeRequest request) {
        long startTime = System.currentTimeMillis();
        
        LOG.infof("Simple API - Solicitud recibida: %s a %s, monto: %s", 
                 request.from(), request.to(), request.value());
        
        // Validar monedas soportadas
        if (!SupportedCurrency.isSupported(request.from()) || 
            !SupportedCurrency.isSupported(request.to())) {
            
            LOG.warnf("Monedas no soportadas: %s -> %s", request.from(), request.to());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Monedas no soportadas\"}")
                    .build();
        }
        
        // Validar que no sean la misma moneda
        if (request.from().equalsIgnoreCase(request.to())) {
            LOG.warn("Intento de conversión de la misma moneda");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"No se puede convertir la misma moneda\"}")
                    .build();
        }
        
        // Simular delay de procesamiento realista
        simulateProcessingDelay();
        
        // Generar tasa aleatoria
        BigDecimal rate = rateGenerator.generateRate(request.from(), request.to());
        
        long processingTime = System.currentTimeMillis() - startTime;
        LOG.infof("Simple API - Tasa generada: %s, tiempo: %dms", rate, processingTime);
        
        SimpleExchangeResponse response = new SimpleExchangeResponse(rate);
        return Response.ok(response).build();
    }
    
    private void simulateProcessingDelay() {
        try {
            int delay = delayMin + (int)(Math.random() * (delayMax - delayMin));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Delay simulado interrumpido");
        }
    }
}