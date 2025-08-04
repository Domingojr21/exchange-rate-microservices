package com.exchangerate.resources;

import org.jboss.logging.Logger;

import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ApiResponseConstants;
import com.exchangerate.models.response.ApiResponseWrapper;
import com.exchangerate.models.response.ExchangeRateResponse;
import com.exchangerate.services.contracts.IExchangeRateService;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Recurso REST para el servicio de tipos de cambio.
 */
@Path("/api/v1/exchange")
@ApplicationScoped
public class ExchangeRateResource {
    
    private static final Logger LOG = Logger.getLogger(ExchangeRateResource.class);
    
    @Inject
    IExchangeRateService exchangeRateService;
    
    /**
     * Obtiene la mejor tasa de cambio entre los proveedores disponibles.
     *
     * @param request solicitud con datos de conversión
     * @return respuesta con la mejor tasa encontrada
     */
    @POST
    @Path("/best-rate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getBestExchangeRate(@Valid ExchangeRateRequest request) {
        
        if (request == null) {
            LOG.warn("Solicitud recibida con body nulo");
            return Uni.createFrom().item(
                Response.status(ApiResponseConstants.HTTP_BAD_REQUEST)
                    .entity(ApiResponseWrapper.badRequest("El cuerpo de la solicitud es requerido"))
                    .build()
            );
        }
        
        if (request.sourceCurrency() == null || request.sourceCurrency().trim().isEmpty()) {
            LOG.warn("Solicitud recibida sin moneda de origen");
            return Uni.createFrom().item(
                Response.status(ApiResponseConstants.HTTP_BAD_REQUEST)
                    .entity(ApiResponseWrapper.badRequest("La moneda de origen es requerida"))
                    .build()
            );
        }
        
        if (request.targetCurrency() == null || request.targetCurrency().trim().isEmpty()) {
            LOG.warn("Solicitud recibida sin moneda de destino");
            return Uni.createFrom().item(
                Response.status(ApiResponseConstants.HTTP_BAD_REQUEST)
                    .entity(ApiResponseWrapper.badRequest("La moneda de destino es requerida"))
                    .build()
            );
        }
        
        if (request.amount() == null) {
            LOG.warn("Solicitud recibida sin monto");
            return Uni.createFrom().item(
                Response.status(ApiResponseConstants.HTTP_BAD_REQUEST)
                    .entity(ApiResponseWrapper.badRequest("El monto es requerido"))
                    .build()
            );
        }
    
        LOG.infof("Solicitud de tipo de cambio recibida: %s %s a %s", 
                 request.amount(), request.sourceCurrency(), request.targetCurrency());
        
        return exchangeRateService.getBestExchangeRate(request)
            .onItem().transform(exchangeResponse -> {
                if (exchangeResponse.bestRate() == null) {
                    LOG.warn("No hay proveedores de tipo de cambio disponibles");
                    return Response.status(ApiResponseConstants.HTTP_SERVICE_UNAVAILABLE)
                        .entity(ApiResponseWrapper.serviceUnavailable(
                            ApiResponseConstants.PROVIDER_NOT_AVAILABLE,
                            exchangeResponse))
                        .build();
                }
                
                LOG.infof("Mejor tasa encontrada: %s desde %s, convertido: %s", 
                         exchangeResponse.bestRate(), 
                         exchangeResponse.provider(), 
                         exchangeResponse.convertedAmount());
                
                return Response.ok(ApiResponseWrapper.success(exchangeResponse)).build();
            })
            .onFailure().invoke(throwable -> 
                LOG.errorf(throwable, "Error procesando solicitud de tipo de cambio: %s", throwable.getMessage())
            )
            .onFailure().recoverWithItem(throwable -> {
                String userFriendlyMessage = translateErrorMessage(throwable);
                return Response.status(ApiResponseConstants.HTTP_BAD_REQUEST)
                    .entity(ApiResponseWrapper.badRequest(userFriendlyMessage))
                    .build();
            });
    }
    
    /**
     * Traduce mensajes técnicos a mensajes amigables en español.
     */
    private String translateErrorMessage(Throwable throwable) {
        String message = throwable.getMessage();
        
        if (message == null) {
            return ApiResponseConstants.SERVER_ERROR;
        }
        
        if (message.contains("timed out") || message.contains("timeout")) {
            return "El servicio está experimentando demoras. Por favor, intente nuevamente en unos momentos.";
        }
        
        if (message.contains("Connection") || message.contains("connection") || 
            message.contains("Name or service not known") || message.contains("refused")) {
            return "Servicio temporalmente no disponible. Por favor, intente nuevamente más tarde.";
        }
        
        if (message.contains("CircuitBreaker") || message.contains("circuit breaker")) {
            return "El servicio está temporalmente suspendido por alta demanda. Por favor, intente nuevamente en unos minutos.";
        }
        
        if (message.contains("inválidos") || message.contains("invalid") || 
            message.contains("Códigos de moneda")) {
            return "Las monedas especificadas no son válidas. Monedas soportadas: USD, EUR, MXN, DOP.";
        }
        
        if (message.contains("Amount") || message.contains("monto")) {
            return ApiResponseConstants.INVALID_AMOUNT;
        }
        
        return "Error procesando la solicitud. Por favor, verifique los datos e intente nuevamente.";
    }
}