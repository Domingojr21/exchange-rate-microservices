package com.exchangerate.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import com.exchangerate.models.dto.api2.XmlExchangeRequest;
import com.exchangerate.models.dto.api2.XmlExchangeResponse;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Cliente REST para el proveedor de tipo de cambio con formato XML.
 * Maneja la comunicación con servicios bancarios tradicionales
 * que utilizan estándares XML para intercambio de datos.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@RegisterRestClient(configKey = "xml-exchange-client")
@ApplicationScoped
@Path("/convert")
public interface XmlExchangeClient {
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    Uni<XmlExchangeResponse> getExchangeRate(XmlExchangeRequest request);
}