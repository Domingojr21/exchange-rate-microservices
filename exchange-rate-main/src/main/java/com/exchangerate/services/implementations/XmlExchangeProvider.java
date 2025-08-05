package com.exchangerate.services.implementations;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.SECONDS;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ApiResponse;
import com.exchangerate.services.contracts.IExchangeRateProvider;
import com.exchangerate.utils.CurrencyUtils;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;

/**
 * Proveedor XML que usa HTTP Client para conectar con XML Exchange API.
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

    @ConfigProperty(name = "api.xml-exchange.path", defaultValue = "/convert")
    String xmlServicePath;

    @ConfigProperty(name = "api.xml.username")
    String username;

    @ConfigProperty(name = "api.xml.password")
    String password;

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Retry(maxRetries = 1, delay = 100, retryOn = { IOException.class, TimeoutException.class }, abortOn = {
            ConnectException.class, UnknownHostException.class })
    @CircuitBreaker(requestVolumeThreshold = 2, failureRatio = 0.5, delay = 200, skipOn = { ConnectException.class,
            UnknownHostException.class })
    @Timeout(value = 1, unit = SECONDS)
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
                request.amount());

        // Construir la URL completa
        String fullUrl = xmlServiceUrl + xmlServicePath;

        return Uni.createFrom().emitter(emitter -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(100))
                        .build();

                // Crear encabezado de autenticación básica
                String auth = username + ":" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(fullUrl))
                        .header("Content-Type", MediaType.APPLICATION_XML)
                        .header("Accept", MediaType.APPLICATION_XML)
                        .header("Authorization", "Basic " + encodedAuth)
                        .POST(HttpRequest.BodyPublishers.ofString(xmlPayload))
                        .build();

                HttpResponse<String> response = client.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String xmlResponse = response.body();

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