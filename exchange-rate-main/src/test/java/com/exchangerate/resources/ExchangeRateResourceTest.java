package com.exchangerate.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ExchangeRateResponse;
import com.exchangerate.services.contracts.IExchangeRateService;
import com.exchangerate.utils.TestConstants;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;

@QuarkusTest
class ExchangeRateResourceTest {

    @InjectMock
    IExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        ExchangeRateResponse defaultResponse = new ExchangeRateResponse(
            new BigDecimal("0.87"), 
            new BigDecimal("87.00"), 
            TestConstants.ADVANCED_PROVIDER, 
            TestConstants.RESPONSE_TIME, 
            TestConstants.SUCCESSFUL_PROVIDERS_ALL, 
            TestConstants.TOTAL_PROVIDERS
        );

        when(exchangeRateService.getBestExchangeRate(any(ExchangeRateRequest.class)))
            .thenReturn(Uni.createFrom().item(defaultResponse));
    }

    @Test
    void testGetBestExchangeRate_USD_EUR() {
        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.EUR, 
            TestConstants.AMOUNT_100
        );

        ExchangeRateResponse eurResponse = new ExchangeRateResponse(
            TestConstants.RATE_USD_EUR, 
            TestConstants.CONVERTED_USD_EUR, 
            TestConstants.SIMPLE_PROVIDER, 
            TestConstants.RESPONSE_TIME, 
            TestConstants.SUCCESSFUL_PROVIDERS_ALL, 
            TestConstants.TOTAL_PROVIDERS
        );
        when(exchangeRateService.getBestExchangeRate(any(ExchangeRateRequest.class)))
            .thenReturn(Uni.createFrom().item(eurResponse));

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.bestRate", notNullValue())
            .body("data.convertedAmount", notNullValue())
            .body("data.provider", notNullValue())
            .body("data.successfulProviders", equalTo(TestConstants.SUCCESSFUL_PROVIDERS_ALL))
            .body("data.totalProviders", equalTo(TestConstants.TOTAL_PROVIDERS));
    }

    @Test
    void testGetBestExchangeRate_USD_MXN() {
        ExchangeRateResponse mxnResponse = new ExchangeRateResponse(
            TestConstants.RATE_USD_MXN, 
            TestConstants.CONVERTED_USD_MXN, 
            TestConstants.XML_PROVIDER, 
            TestConstants.RESPONSE_TIME, 
            TestConstants.SUCCESSFUL_PROVIDERS_ALL, 
            TestConstants.TOTAL_PROVIDERS
        );
        when(exchangeRateService.getBestExchangeRate(any(ExchangeRateRequest.class)))
            .thenReturn(Uni.createFrom().item(mxnResponse));

        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.MXN, 
            TestConstants.AMOUNT_50
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.convertedAmount", greaterThan(800.0f));
    }

    @Test
    void testGetBestExchangeRate_EUR_DOP() {
        ExchangeRateResponse dopResponse = new ExchangeRateResponse(
            TestConstants.RATE_EUR_DOP, 
            TestConstants.CONVERTED_EUR_DOP, 
            TestConstants.ADVANCED_PROVIDER, 
            TestConstants.RESPONSE_TIME, 
            TestConstants.SUCCESSFUL_PROVIDERS_ALL, 
            TestConstants.TOTAL_PROVIDERS
        );
        when(exchangeRateService.getBestExchangeRate(any(ExchangeRateRequest.class)))
            .thenReturn(Uni.createFrom().item(dopResponse));

        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.EUR, 
            TestConstants.DOP, 
            TestConstants.AMOUNT_25
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.convertedAmount", greaterThan(1500.0f));
    }

    @Test
    void testGetBestExchangeRate_UnsupportedCurrency() {
        when(exchangeRateService.getBestExchangeRate(any(ExchangeRateRequest.class)))
            .thenReturn(Uni.createFrom().failure(new IllegalArgumentException("Códigos de moneda inválidos proporcionados")));

        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            "GBP", 
            TestConstants.AMOUNT_100
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400))
            .body("message", containsString("Las monedas especificadas no son válidas"));
    }

    @Test
    void testGetBestExchangeRate_SameCurrency() {
        when(exchangeRateService.getBestExchangeRate(any(ExchangeRateRequest.class)))
            .thenReturn(Uni.createFrom().failure(new IllegalArgumentException("Códigos de moneda inválidos proporcionados")));

        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.USD, 
            TestConstants.AMOUNT_100
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void testGetBestExchangeRate_InvalidAmount() {
        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.EUR, 
            new BigDecimal("-100.00")
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void testGetBestExchangeRate_ZeroAmount() {
        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.EUR, 
            BigDecimal.ZERO
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }

    @Test
    void testGetBestExchangeRate_AllSupportedPairs() {
        String[] currencies = {TestConstants.USD, TestConstants.EUR, TestConstants.MXN, TestConstants.DOP};
        
        for (String from : currencies) {
            for (String to : currencies) {
                if (!from.equals(to)) {
                    ExchangeRateRequest request = new ExchangeRateRequest(
                        from, 
                        to, 
                        TestConstants.AMOUNT_100
                    );
                    
                    given()
                        .contentType(ContentType.JSON)
                        .body(request)
                    .when()
                        .post("/api/v1/exchange/best-rate")
                    .then()
                        .statusCode(200)
                        .body("code", equalTo(200))
                        .body("data.bestRate", greaterThan(0.0f))
                        .body("data.convertedAmount", greaterThan(0.0f));
                }
            }
        }
    }

    @Test
    void testGetBestExchangeRate_SomeProvidersFail() {
        ExchangeRateResponse partialResponse = new ExchangeRateResponse(
            new BigDecimal("0.87"), 
            new BigDecimal("87.00"), 
            TestConstants.ADVANCED_PROVIDER, 
            TestConstants.RESPONSE_TIME, 
            TestConstants.SUCCESSFUL_PROVIDERS_SOME, 
            TestConstants.TOTAL_PROVIDERS
        );
        when(exchangeRateService.getBestExchangeRate(any(ExchangeRateRequest.class)))
            .thenReturn(Uni.createFrom().item(partialResponse));

        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.EUR, 
            TestConstants.AMOUNT_100
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.successfulProviders", equalTo(TestConstants.SUCCESSFUL_PROVIDERS_SOME))
            .body("data.totalProviders", equalTo(TestConstants.TOTAL_PROVIDERS))
            .body("data.provider", equalTo(TestConstants.ADVANCED_PROVIDER));
    }

    @Test
    void testGetBestExchangeRate_NoProvidersAvailable() {
        ExchangeRateResponse noProviderResponse = new ExchangeRateResponse(
            null, 
            null, 
            TestConstants.NO_PROVIDER, 
            TestConstants.RESPONSE_TIME, 
            TestConstants.SUCCESSFUL_PROVIDERS_NONE, 
            TestConstants.TOTAL_PROVIDERS
        );
        when(exchangeRateService.getBestExchangeRate(any(ExchangeRateRequest.class)))
            .thenReturn(Uni.createFrom().item(noProviderResponse));

        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.EUR, 
            TestConstants.AMOUNT_100
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(503)
            .body("code", equalTo(503))
            .body("data.successfulProviders", equalTo(TestConstants.SUCCESSFUL_PROVIDERS_NONE))
            .body("data.totalProviders", equalTo(TestConstants.TOTAL_PROVIDERS))
            .body("data.provider", equalTo(TestConstants.NO_PROVIDER));
    }
    
    @Test
    void testGetBestExchangeRate_NullRequest() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400))
            .body("message", notNullValue());
    }
    
    @Test
    void testGetBestExchangeRate_NullSourceCurrency() {
        ExchangeRateRequest request = new ExchangeRateRequest(
            null, 
            TestConstants.EUR, 
            TestConstants.AMOUNT_100
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400))
            .body("message", containsString("sourceCurrency: La moneda de origen es obligatoria"));
    }
    
    @Test
    void testGetBestExchangeRate_EmptySourceCurrency() {
        ExchangeRateRequest request = new ExchangeRateRequest(
            "", 
            TestConstants.EUR, 
            TestConstants.AMOUNT_100
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }
    
    @Test
    void testGetBestExchangeRate_NullTargetCurrency() {
        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            null, 
            TestConstants.AMOUNT_100
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400))
            .body("message", containsString("targetCurrency: La moneda de destino es obligatoria"));
    }
    
    @Test
    void testGetBestExchangeRate_EmptyTargetCurrency() {
        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            "  ", 
            TestConstants.AMOUNT_100
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400));
    }
    
    @Test
    void testGetBestExchangeRate_NullAmount() {
        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.EUR, 
            null
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400))
            .body("message", containsString("El monto es obligatorio"));
    }
    
    @Test
    void testGetBestExchangeRate_ServiceThrowsException() {
        when(exchangeRateService.getBestExchangeRate(any(ExchangeRateRequest.class)))
            .thenReturn(Uni.createFrom().failure(new RuntimeException("Error inesperado")));

        ExchangeRateRequest request = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.EUR, 
            TestConstants.AMOUNT_100
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/exchange/best-rate")
        .then()
            .statusCode(400)
            .body("code", equalTo(400))
            .body("message", notNullValue());
    }
}