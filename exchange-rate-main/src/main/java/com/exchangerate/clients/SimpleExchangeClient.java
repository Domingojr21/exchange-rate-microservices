package com.exchangerate.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.exchangerate.models.dto.api1.SimpleExchangeRequest;
import com.exchangerate.models.dto.api1.SimpleExchangeResponse;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Cliente REST para el proveedor de tipo de cambio con formato JSON simple.
 * Maneja la comunicación con servicios externos que utilizan
 * estructura de datos plana para conversión de monedas.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterRestClient(configKey = "simple-exchange-client")
@ApplicationScoped
@Path("/exchange")
public interface SimpleExchangeClient {
    
    /**
     * Obtiene la tasa de cambio del proveedor simple.
     * 
     * @param request solicitud con datos de conversión
     * @return respuesta asíncrona con la tasa calculada
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<SimpleExchangeResponse> getExchangeRate(SimpleExchangeRequest request);
}