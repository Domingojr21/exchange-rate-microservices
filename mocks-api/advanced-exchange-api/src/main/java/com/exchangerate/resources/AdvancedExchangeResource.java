package com.exchangerate.resources;

import java.math.BigDecimal;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.exchangerate.models.AdvancedExchangeRequest;
import com.exchangerate.models.AdvancedExchangeResponse;
import com.exchangerate.models.ExchangeResultData;
import com.exchangerate.models.enums.SupportedCurrency;
import com.exchangerate.services.RandomRateGenerator;
import com.exchangerate.utils.CurrencyUtils;

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
 * Recurso REST para Advanced Exchange API.
 * Servicios fintech de alta frecuencia especializados en DOP.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@Path("/rate")
@ApplicationScoped
@RolesAllowed("user")
public class AdvancedExchangeResource {
    
    private static final Logger LOG = Logger.getLogger(AdvancedExchangeResource.class);
    
    @Inject
    RandomRateGenerator rateGenerator;
    
    @ConfigProperty(name = "provider.response.delay.min")
    int delayMin;
    
    @ConfigProperty(name = "provider.response.delay.max")
    int delayMax;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExchangeRate(AdvancedExchangeRequest request) {
        long startTime = System.currentTimeMillis();
        
        LOG.infof("Advanced API - Solicitud recibida: %s a %s, cantidad: %s", 
                 request.exchange().sourceCurrency(), 
                 request.exchange().targetCurrency(), 
                 request.exchange().quantity());
        
        // Validaciones
        if (!SupportedCurrency.isSupported(request.exchange().sourceCurrency()) || 
            !SupportedCurrency.isSupported(request.exchange().targetCurrency())) {
            
            LOG.warnf("Monedas no soportadas: %s -> %s", 
                     request.exchange().sourceCurrency(), request.exchange().targetCurrency());
                     
            AdvancedExchangeResponse errorResponse = new AdvancedExchangeResponse(
                400, "Monedas no soportadas", null
            );
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }
        
        if (request.exchange().sourceCurrency().equalsIgnoreCase(request.exchange().targetCurrency())) {
            AdvancedExchangeResponse errorResponse = new AdvancedExchangeResponse(
                400, "No se puede convertir la misma moneda", null
            );
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }
        
        // Simular procesamiento fintech (más rápido)
        simulateProcessingDelay();
        
        // Generar tasa y calcular total
        BigDecimal rate = rateGenerator.generateRate(
            request.exchange().sourceCurrency(), 
            request.exchange().targetCurrency()
        );
        BigDecimal total = CurrencyUtils.calculateConvertedAmount(request.exchange().quantity(), rate);
        
        long processingTime = System.currentTimeMillis() - startTime;
        LOG.infof("Advanced API - Total calculado: %s, tiempo: %dms", total, processingTime);
        
        ExchangeResultData resultData = new ExchangeResultData(total);
        AdvancedExchangeResponse response = new AdvancedExchangeResponse(
            200, "Conversión exitosa", resultData
        );
        
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