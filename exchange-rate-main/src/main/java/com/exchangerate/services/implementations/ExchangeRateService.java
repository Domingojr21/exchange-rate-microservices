package com.exchangerate.services.implementations;

import java.util.List;

import org.jboss.logging.Logger;

import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ExchangeRateResponse;
import com.exchangerate.services.contracts.IExchangeRateComparator;
import com.exchangerate.services.contracts.IExchangeRateProvider;
import com.exchangerate.services.contracts.IExchangeRateService;
import com.exchangerate.utils.CurrencyUtils;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Servicio principal para comparación de tipos de cambio mejorado.
 * Orquesta las llamadas a múltiples proveedores de APIs,
 * maneja la tolerancia a fallos y selecciona la mejor oferta.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@ApplicationScoped
public class ExchangeRateService implements IExchangeRateService {
    
    private static final Logger LOG = Logger.getLogger(ExchangeRateService.class);
    
    @Inject
    SimpleExchangeProvider simpleProvider;
    
    @Inject
    XmlExchangeProvider xmlProvider;
    
    @Inject
    AdvancedExchangeProvider advancedProvider;
    
    @Inject
    IExchangeRateComparator comparator;
    
    @Override
    public Uni<ExchangeRateResponse> getBestExchangeRate(ExchangeRateRequest request) {
        long startTime = System.currentTimeMillis();
        
        LOG.infof("Iniciando comparación de tipos de cambio para %s a %s, monto: %s", 
                 request.sourceCurrency(), request.targetCurrency(), request.amount());
        
        // Validar monedas
        if (!CurrencyUtils.isValidCurrencyPair(request.sourceCurrency(), request.targetCurrency())) {
            return Uni.createFrom().failure(
                new IllegalArgumentException("Códigos de moneda inválidos proporcionados")
            );
        }
        
        // Obtener todos los proveedores
        List<IExchangeRateProvider> providers = List.of(simpleProvider, xmlProvider, advancedProvider);
        
        // Llamar a todas las APIs en paralelo con mejor manejo de errores
        List<Uni<com.exchangerate.models.response.ApiResponse>> apiCalls = providers.stream()
            .map(provider -> {
                return provider.getExchangeRate(request)
                    .onFailure().recoverWithItem(throwable -> {
                        // Recuperar graciosamente de cualquier fallo
                        long responseTime = System.currentTimeMillis() - startTime;
                        String errorMsg = translateProviderError(throwable);
                        
                        LOG.warnf("Proveedor %s falló: %s", provider.getProviderName(), errorMsg);
                        
                        return com.exchangerate.models.response.ApiResponse.failure(
                            provider.getProviderName(), errorMsg, responseTime
                        );
                    });
            })
            .toList();
        
        return Uni.combine().all().unis(apiCalls)
            .combinedWith(responses -> {
                long totalResponseTime = System.currentTimeMillis() - startTime;
                
                @SuppressWarnings("unchecked")
                List<com.exchangerate.models.response.ApiResponse> apiResponses = 
                    (List<com.exchangerate.models.response.ApiResponse>) responses;
                
                LOG.infof("Todas las llamadas a APIs completadas en %dms", totalResponseTime);
                
                // Logging detallado de respuestas
                long successCount = apiResponses.stream().mapToLong(r -> r.successful() ? 1 : 0).sum();
                LOG.infof("Resumen de APIs: %d exitosas, %d fallidas de %d totales", 
                         successCount, apiResponses.size() - successCount, apiResponses.size());
                
                // Log individual de cada proveedor
                apiResponses.forEach(response -> {
                    if (response.successful()) {
                        LOG.infof("✅ %s: tasa=%s, convertido=%s, tiempo=%dms", 
                                 response.provider(), response.rate(), 
                                 response.convertedAmount(), response.responseTimeMs());
                    } else {
                        LOG.warnf("❌ %s: error=%s, tiempo=%dms", 
                                 response.provider(), response.errorMessage(), response.responseTimeMs());
                    }
                });
                
                // Buscar mejor tasa
                return comparator.selectBestRate(request, apiResponses, totalResponseTime);
            })
            .onFailure().recoverWithItem(throwable -> {
                long totalResponseTime = System.currentTimeMillis() - startTime;
                LOG.errorf(throwable, "Error crítico en el servicio de tipos de cambio");
                
                return new ExchangeRateResponse(
                    null, null, "ERROR_SERVICIO", totalResponseTime, 0, 3
                );
            });
    }
    
    /**
     * Traduce errores técnicos de providers a mensajes más descriptivos
     */
    private String translateProviderError(Throwable throwable) {
        String message = throwable.getMessage();
        
        if (message == null) {
            return "Error de conexión";
        }
        
        if (message.contains("timed out") || message.contains("timeout")) {
            return "Tiempo de espera agotado";
        }
        
        if (message.contains("Connection") || message.contains("service not known")) {
            return "Servicio no disponible";
        }
        
        if (message.contains("CircuitBreaker")) {
            return "Circuito abierto por fallos repetidos";
        }
        
        return "Error de proveedor";
    }
}