package com.exchangerate.resources;

import java.math.BigDecimal;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.exchangerate.models.XmlExchangeRequest;
import com.exchangerate.models.XmlExchangeResponse;
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
 * Recurso REST para XML Exchange API.
 * Servicios bancarios tradicionales con formato XML.
 */
@Path("/convert")
@ApplicationScoped
@RolesAllowed("user")
public class XmlExchangeResource {
    
    private static final Logger LOG = Logger.getLogger(XmlExchangeResource.class);
    
    @Inject
    RandomRateGenerator rateGenerator;
    
    @ConfigProperty(name = "provider.response.delay.min", defaultValue = "100")
    int delayMin;
    
    @ConfigProperty(name = "provider.response.delay.max", defaultValue = "250")
    int delayMax;
    
    /**
     * Endpoint para convertir monedas usando formato XML.
     * 
     * @param request solicitud XML con datos de conversión
     * @return respuesta con el resultado de la conversión
     */
    @POST
    @Consumes({"application/xml", "text/xml"})
    @Produces({"application/xml", "text/xml"})
    public Response convertCurrency(XmlExchangeRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            LOG.infof("XML API - Solicitud recibida: %s a %s, monto: %s", 
                     request.getFrom(), request.getTo(), request.getAmount());
                     
            // Validaciones
            if (request.getFrom() == null || request.getTo() == null || request.getAmount() == null) {
                LOG.warn("Datos de solicitud incompletos");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new XmlExchangeResponse(null))
                        .build();
            }
            
            if (!SupportedCurrency.isSupported(request.getFrom()) || 
                !SupportedCurrency.isSupported(request.getTo())) {
                
                LOG.warnf("Monedas no soportadas: %s -> %s", request.getFrom(), request.getTo());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new XmlExchangeResponse(null))
                        .build();
            }
            
            if (request.getFrom().equalsIgnoreCase(request.getTo())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new XmlExchangeResponse(null))
                        .build();
            }
            
            // Simular procesamiento bancario (más lento)
            simulateProcessingDelay();
            
            // Generar tasa y calcular resultado
            BigDecimal rate = rateGenerator.generateRate(request.getFrom(), request.getTo());
            BigDecimal result = CurrencyUtils.calculateConvertedAmount(request.getAmount(), rate);
            
            long processingTime = System.currentTimeMillis() - startTime;
            LOG.infof("XML API - Resultado: %s, tiempo: %dms", result, processingTime);
            
            XmlExchangeResponse response = new XmlExchangeResponse(result);
            return Response.ok(response).build();
                    
        } catch (Exception e) {
            LOG.errorf("Error procesando solicitud XML: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new XmlExchangeResponse(null))
                    .build();
        }
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