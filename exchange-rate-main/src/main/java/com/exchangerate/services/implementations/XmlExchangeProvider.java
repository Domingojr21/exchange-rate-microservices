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

import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Proveedor XML que usa REST Client para conectar con XML Exchange API.
 * Especializado en conversiones con pesos mexicanos.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@ApplicationScoped
public class XmlExchangeProvider implements IExchangeRateProvider {
    
   private static final Logger LOG = Logger.getLogger(XmlExchangeProvider.class);
    private static final String PROVIDER_NAME = "XML_BANKING_PROVIDER";
    
    @ConfigProperty(name = "quarkus.rest-client.xml-exchange-client.url")
    String xmlServiceUrl;
    
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
        
        // Crear el XML manualmente
        String xmlPayload = String.format(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<XML>\n" +
            "    <From>%s</From>\n" +
            "    <To>%s</To>\n" +
            "    <Amount>%s</Amount>\n" +
            "</XML>",
            request.sourceCurrency(),
            request.targetCurrency(),
            request.amount()
        );
        
        LOG.debugf("XML Request payload: %s", xmlPayload);
        
        return Uni.createFrom().emitter(emitter -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
                    
                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(xmlServiceUrl + "/convert"))
                    .header("Content-Type", MediaType.APPLICATION_XML)
                    .header("Accept", MediaType.APPLICATION_XML)
                    .POST(HttpRequest.BodyPublishers.ofString(xmlPayload))
                    .build();
                    
                HttpResponse<String> response = client.send(httpRequest, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    String xmlResponse = response.body();
                    LOG.debugf("XML Response: %s", xmlResponse);
                    
                    // Parse XML response
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));
                    Element root = document.getDocumentElement();
                    String resultStr = root.getElementsByTagName("Result").item(0).getTextContent();
                    BigDecimal result = new BigDecimal(resultStr);
                    
                    long responseTime = System.currentTimeMillis() - startTime;
                    BigDecimal rate = CurrencyUtils.calculateRate(request.amount(), result);
                    
                    LOG.infof("%s éxito: tasa=%s, convertido=%s, tiempo=%dms", 
                             PROVIDER_NAME, rate, result, responseTime);
                    
                    emitter.complete(ApiResponse.success(PROVIDER_NAME, rate, result, responseTime));
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