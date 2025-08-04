package com.exchangerate.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.exchangerate.models.dto.api3.AdvancedExchangeRequest;
import com.exchangerate.models.dto.api3.AdvancedExchangeResponse;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Cliente REST para el proveedor de tipo de cambio con formato JSON avanzado.
 * Maneja la comunicación con servicios financieros especializados
 * que utilizan estructuras de datos complejas y anidadas.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterRestClient(configKey = "advanced-exchange-client")
@ApplicationScoped
@Path("/rate")
public interface AdvancedExchangeClient {
    
    /**
     * Obtiene la tasa de cambio del proveedor avanzado.
     * 
     * @param request solicitud con datos de conversión en formato JSON anidado
     * @return respuesta asíncrona con resultado completo y metadatos
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<AdvancedExchangeResponse> getExchangeRate(AdvancedExchangeRequest request);
}