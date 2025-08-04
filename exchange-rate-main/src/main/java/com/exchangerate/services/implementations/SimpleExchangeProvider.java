package com.exchangerate.services.implementations;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.exchangerate.clients.SimpleExchangeClient;
import com.exchangerate.models.dto.api1.SimpleExchangeRequest;
import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ApiResponse;
import com.exchangerate.services.contracts.IExchangeRateProvider;
import com.exchangerate.utils.CurrencyUtils;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Proveedor simple que usa REST Client para conectar con Simple Exchange API.
 * Especializado en conversiones principales USD/EUR y EUR/USD.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@ApplicationScoped
public class SimpleExchangeProvider implements IExchangeRateProvider {
    
    private static final Logger LOG = Logger.getLogger(SimpleExchangeProvider.class);
    private static final String PROVIDER_NAME = "SIMPLE_JSON_PROVIDER";
    
    @Inject
    @RestClient
    SimpleExchangeClient simpleExchangeClient;
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
    
    @Override
    @Retry(maxRetries = 2, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 5000)
    @Timeout(value = 5, unit = java.time.temporal.ChronoUnit.SECONDS)
    public Uni<ApiResponse> getExchangeRate(ExchangeRateRequest request) {
        long startTime = System.currentTimeMillis();
        
        LOG.infof("Llamando a %s para %s a %s, monto: %s", 
                 PROVIDER_NAME, request.sourceCurrency(), request.targetCurrency(), request.amount());
        
        // Crear request para el microservicio
        SimpleExchangeRequest simpleRequest = new SimpleExchangeRequest(
            request.sourceCurrency(),
            request.targetCurrency(), 
            request.amount()
        );
        
        return simpleExchangeClient.getExchangeRate(simpleRequest)
            .onItem().transform(response -> {
                long responseTime = System.currentTimeMillis() - startTime;
                var convertedAmount = CurrencyUtils.calculateConvertedAmount(request.amount(), response.rate());
                
                LOG.infof("%s éxito: tasa=%s, convertido=%s, tiempo=%dms", 
                         PROVIDER_NAME, response.rate(), convertedAmount, responseTime);
                
                return ApiResponse.success(PROVIDER_NAME, response.rate(), convertedAmount, responseTime);
            })
            .onFailure().recoverWithItem(throwable -> {
                long responseTime = System.currentTimeMillis() - startTime;
                String errorMsg = throwable.getMessage();
                
                LOG.errorf("%s falló: %s, tiempo=%dms", PROVIDER_NAME, errorMsg, responseTime);
                
                return ApiResponse.failure(PROVIDER_NAME, errorMsg, responseTime);
            });
    }
}