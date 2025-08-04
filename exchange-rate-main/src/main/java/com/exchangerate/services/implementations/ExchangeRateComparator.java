package com.exchangerate.services.implementations;

import java.util.Comparator;
import java.util.List;

import org.jboss.logging.Logger;

import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ApiResponse;
import com.exchangerate.models.response.ExchangeRateResponse;
import com.exchangerate.services.contracts.IExchangeRateComparator;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExchangeRateComparator implements IExchangeRateComparator {
    
    private static final Logger LOG = Logger.getLogger(ExchangeRateComparator.class);
    
    @Override
    public ExchangeRateResponse selectBestRate(ExchangeRateRequest request, List<ApiResponse> responses, long totalResponseTime) {
        LOG.infof("Comparing %d API responses for best exchange rate", responses.size());
        
        List<ApiResponse> successfulResponses = responses.stream()
            .filter(ApiResponse::successful)
            .toList();
        
        LOG.infof("Found %d successful responses out of %d total", successfulResponses.size(), responses.size());
        
        if (successfulResponses.isEmpty()) {
            LOG.warn("No successful API responses available");
            return createNoDataResponse(responses.size(), totalResponseTime);
        }
        
        // Find the response with the highest converted amount (best deal for the customer)
        ApiResponse bestResponse = successfulResponses.stream()
            .max(Comparator.comparing(ApiResponse::convertedAmount))
            .orElseThrow(() -> new IllegalStateException("Expected at least one successful response"));
        
        LOG.infof("Best rate found from %s: rate=%s, converted=%s", 
                 bestResponse.provider(), bestResponse.rate(), bestResponse.convertedAmount());
        
        return new ExchangeRateResponse(
            bestResponse.rate(),
            bestResponse.convertedAmount(),
            bestResponse.provider(),
            totalResponseTime,
            successfulResponses.size(),
            responses.size()
        );
    }
    
    private ExchangeRateResponse createNoDataResponse(int totalProviders, long totalResponseTime) {
        return new ExchangeRateResponse(
            null,
            null,
            "NO_PROVIDER_AVAILABLE",
            totalResponseTime,
            0,
            totalProviders
        );
    }
}