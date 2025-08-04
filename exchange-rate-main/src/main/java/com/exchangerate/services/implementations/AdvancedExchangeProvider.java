package com.exchangerate.services.implementations;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.exchangerate.clients.AdvancedExchangeClient;
import com.exchangerate.models.dto.api3.AdvancedExchangeRequest;
import com.exchangerate.models.dto.api3.ExchangeDetails;
import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ApiResponse;
import com.exchangerate.services.contracts.IExchangeRateProvider;
import com.exchangerate.utils.CurrencyUtils;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Proveedor avanzado que usa REST Client para conectar con Advanced Exchange API.
 * Especializado en peso dominicano y conversiones emergentes.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@ApplicationScoped
public class AdvancedExchangeProvider implements IExchangeRateProvider {
    
    private static final Logger LOG = Logger.getLogger(AdvancedExchangeProvider.class);
    private static final String PROVIDER_NAME = "ADVANCED_FINTECH_PROVIDER";
    
    @Inject
    @RestClient
    AdvancedExchangeClient advancedExchangeClient;
    
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
        
        ExchangeDetails exchangeDetails = new ExchangeDetails(
            request.sourceCurrency(),
            request.targetCurrency(), 
            request.amount()
        );
        AdvancedExchangeRequest advancedRequest = new AdvancedExchangeRequest(exchangeDetails);
        
        return advancedExchangeClient.getExchangeRate(advancedRequest)
            .onItem().transform(response -> {
                long responseTime = System.currentTimeMillis() - startTime;
                
                if (response.statusCode() != 200 || response.data() == null) {
                    throw new RuntimeException("Invalid response from advanced provider: " + response.message());
                }
                
                var rate = CurrencyUtils.calculateRate(request.amount(), response.data().total());
                var convertedAmount = response.data().total();
                
                LOG.infof("%s éxito: tasa=%s, convertido=%s, tiempo=%dms", 
                         PROVIDER_NAME, rate, convertedAmount, responseTime);
                
                return ApiResponse.success(PROVIDER_NAME, rate, convertedAmount, responseTime);
            })
            .onFailure().recoverWithItem(throwable -> {
                long responseTime = System.currentTimeMillis() - startTime;
                String errorMsg = throwable.getMessage();
                
                LOG.errorf("%s falló: %s, tiempo=%dms", PROVIDER_NAME, errorMsg, responseTime);
                
                return ApiResponse.failure(PROVIDER_NAME, errorMsg, responseTime);
            });
    }
}