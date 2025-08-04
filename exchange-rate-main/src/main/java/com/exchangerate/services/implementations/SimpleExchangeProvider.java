package com.exchangerate.services.implementations;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ApiResponse;
import com.exchangerate.services.contracts.IExchangeRateProvider;
import com.exchangerate.utils.CurrencyUtils;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Proveedor simple que usa HTTP Client para conectar con Simple Exchange API.
 * Especializado en conversiones principales USD/EUR y EUR/USD.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@ApplicationScoped
public class SimpleExchangeProvider implements IExchangeRateProvider {
    
    private static final Logger LOG = Logger.getLogger(SimpleExchangeProvider.class);
    private static final String PROVIDER_NAME = "SIMPLE_JSON_PROVIDER";
    
    @ConfigProperty(name = "quarkus.rest-client.simple-exchange-client.url")
    String simpleServiceUrl;
    
    @ConfigProperty(name = "api.simple-exchange.path", defaultValue = "/exchange")
    String simpleServicePath;
    
    @ConfigProperty(name = "api.simple.username")
    String username;
    
    @ConfigProperty(name = "api.simple.password")
    String password;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
        
        // Crear el JSON para la solicitud
        String jsonRequest = String.format(
            "{\"from\":\"%s\",\"to\":\"%s\",\"value\":%s}",
            request.sourceCurrency(),
            request.targetCurrency(),
            request.amount()
        );
        
        return Uni.createFrom().emitter(emitter -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
                
                // Crear encabezado de autenticación básica
                String auth = username + ":" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                
                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(simpleServiceUrl + simpleServicePath))
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Accept", MediaType.APPLICATION_JSON)
                    .header("Authorization", "Basic " + encodedAuth)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();
                    
                HttpResponse<String> response = client.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    String jsonResponse = response.body();
                    
                    // Analizar respuesta JSON
                    JsonNode rootNode = objectMapper.readTree(jsonResponse);
                    BigDecimal rate = new BigDecimal(rootNode.get("rate").asText());
                    BigDecimal convertedAmount = CurrencyUtils.calculateConvertedAmount(request.amount(), rate);
                    
                    long responseTime = System.currentTimeMillis() - startTime;
                    
                    LOG.infof("%s éxito: tasa=%s, convertido=%s, tiempo=%dms", 
                             PROVIDER_NAME, rate, convertedAmount, responseTime);
                    
                    emitter.complete(ApiResponse.success(PROVIDER_NAME, rate, convertedAmount, responseTime));
                } else {
                    long responseTime = System.currentTimeMillis() - startTime;
                    String errorMsg = "HTTP error: " + response.statusCode() + " - " + response.body();
                    LOG.errorf("%s falló: %s, tiempo=%dms", PROVIDER_NAME, errorMsg, responseTime);
                    emitter.complete(ApiResponse.failure(PROVIDER_NAME, errorMsg, responseTime));
                }
            } catch (Exception e) {
                long responseTime = System.currentTimeMillis() - startTime;
                LOG.errorf("%s falló: %s, tiempo=%dms", PROVIDER_NAME, e.getMessage(), responseTime);
                emitter.complete(ApiResponse.failure(PROVIDER_NAME, e.getMessage(), responseTime));
            }
        });
    }
}