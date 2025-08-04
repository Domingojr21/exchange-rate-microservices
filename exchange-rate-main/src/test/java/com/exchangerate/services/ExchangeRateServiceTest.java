package com.exchangerate.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ApiResponse;
import com.exchangerate.models.response.ExchangeRateResponse;
import com.exchangerate.services.contracts.IExchangeRateComparator;
import com.exchangerate.services.implementations.SimpleExchangeProvider;
import com.exchangerate.services.implementations.XmlExchangeProvider;
import com.exchangerate.services.implementations.AdvancedExchangeProvider;
import com.exchangerate.services.implementations.ExchangeRateService;
import com.exchangerate.utils.TestConstants;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;

@QuarkusTest
class ExchangeRateServiceTest {

    @Mock
    SimpleExchangeProvider api1Provider;

    @Mock
    XmlExchangeProvider api2Provider;

    @Mock
    AdvancedExchangeProvider api3Provider;

    @Mock
    IExchangeRateComparator comparator;

    @InjectMocks
    ExchangeRateService exchangeRateService;

    private ExchangeRateRequest validRequest;
    private ExchangeRateRequest invalidRequest;
    private ExchangeRateRequest sameCurrencyRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Valid request
        validRequest = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.EUR, 
            TestConstants.AMOUNT_100
        );
        
        // Invalid currency request
        invalidRequest = new ExchangeRateRequest(
            "INVALID", 
            TestConstants.EUR, 
            TestConstants.AMOUNT_100
        );
        
        // Same currency request
        sameCurrencyRequest = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.USD, 
            TestConstants.AMOUNT_100
        );
    }

    @Test
    void testGetBestExchangeRate_Success() {
        // Arrange
        ApiResponse api1Response = ApiResponse.success(
            TestConstants.SIMPLE_PROVIDER, 
            TestConstants.RATE_USD_EUR, 
            TestConstants.CONVERTED_USD_EUR, 
            100L
        );
        
        ApiResponse api2Response = ApiResponse.success(
            TestConstants.XML_PROVIDER, 
            new BigDecimal("0.86"), 
            new BigDecimal("86.00"), 
            150L
        );
        
        ApiResponse api3Response = ApiResponse.success(
            TestConstants.ADVANCED_PROVIDER, 
            new BigDecimal("0.84"), 
            new BigDecimal("84.00"), 
            120L
        );

        when(api1Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api1Response));
        when(api2Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api2Response));
        when(api3Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api3Response));

        ExchangeRateResponse expectedResponse = new ExchangeRateResponse(
            new BigDecimal("0.86"), 
            new BigDecimal("86.00"), 
            TestConstants.XML_PROVIDER, 
            200L, 
            3, 
            3
        );

        when(comparator.selectBestRate(any(), any(), anyLong())).thenReturn(expectedResponse);

        // Act
        ExchangeRateResponse result = exchangeRateService.getBestExchangeRate(validRequest)
            .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(TestConstants.XML_PROVIDER, result.provider());
        assertEquals(new BigDecimal("0.86"), result.bestRate());
        assertEquals(new BigDecimal("86.00"), result.convertedAmount());
        assertEquals(3, result.successfulProviders());

        verify(api1Provider, times(1)).getExchangeRate(validRequest);
        verify(api2Provider, times(1)).getExchangeRate(validRequest);
        verify(api3Provider, times(1)).getExchangeRate(validRequest);
        verify(comparator, times(1)).selectBestRate(eq(validRequest), any(), anyLong());
    }

    @Test
    void testGetBestExchangeRate_InvalidCurrency() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            exchangeRateService.getBestExchangeRate(invalidRequest).await().indefinitely();
        });
        
        assertTrue(exception.getMessage().contains("inválidos"));
        
        verify(api1Provider, never()).getExchangeRate(any());
        verify(api2Provider, never()).getExchangeRate(any());
        verify(api3Provider, never()).getExchangeRate(any());
    }
    
    @Test
    void testGetBestExchangeRate_SameCurrency() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            exchangeRateService.getBestExchangeRate(sameCurrencyRequest).await().indefinitely();
        });
        
        assertTrue(exception.getMessage().contains("inválidos"));
        
        verify(api1Provider, never()).getExchangeRate(any());
        verify(api2Provider, never()).getExchangeRate(any());
        verify(api3Provider, never()).getExchangeRate(any());
    }

    @Test
    void testGetBestExchangeRate_SomeProvidersFail() {
        // Arrange
        ApiResponse api1Response = ApiResponse.success(
            TestConstants.SIMPLE_PROVIDER, 
            TestConstants.RATE_USD_EUR, 
            TestConstants.CONVERTED_USD_EUR, 
            100L
        );
        
        ApiResponse api2Response = ApiResponse.failure(
            TestConstants.XML_PROVIDER, 
            "Connection timeout", 
            5000L
        );
        
        ApiResponse api3Response = ApiResponse.success(
            TestConstants.ADVANCED_PROVIDER, 
            new BigDecimal("0.84"), 
            new BigDecimal("84.00"), 
            120L
        );

        when(api1Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api1Response));
        when(api2Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api2Response));
        when(api3Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api3Response));

        ExchangeRateResponse expectedResponse = new ExchangeRateResponse(
            TestConstants.RATE_USD_EUR, 
            TestConstants.CONVERTED_USD_EUR, 
            TestConstants.SIMPLE_PROVIDER, 
            200L, 
            2, 
            3
        );

        when(comparator.selectBestRate(any(), any(), anyLong())).thenReturn(expectedResponse);

        // Act
        ExchangeRateResponse result = exchangeRateService.getBestExchangeRate(validRequest)
            .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(TestConstants.SIMPLE_PROVIDER, result.provider());
        assertEquals(2, result.successfulProviders());
        assertEquals(3, result.totalProviders());

        verify(comparator, times(1)).selectBestRate(eq(validRequest), any(), anyLong());
    }
    
    @Test
    void testGetBestExchangeRate_AllProvidersFail() {
        // Arrange
        ApiResponse api1Response = ApiResponse.failure(
            TestConstants.SIMPLE_PROVIDER, 
            "Service unavailable", 
            100L
        );
        
        ApiResponse api2Response = ApiResponse.failure(
            TestConstants.XML_PROVIDER, 
            "Connection timeout", 
            150L
        );
        
        ApiResponse api3Response = ApiResponse.failure(
            TestConstants.ADVANCED_PROVIDER, 
            "Internal error", 
            120L
        );

        when(api1Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api1Response));
        when(api2Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api2Response));
        when(api3Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api3Response));

        ExchangeRateResponse expectedResponse = new ExchangeRateResponse(
            null, 
            null, 
            TestConstants.NO_PROVIDER, 
            200L, 
            0, 
            3
        );

        when(comparator.selectBestRate(any(), any(), anyLong())).thenReturn(expectedResponse);

        // Act
        ExchangeRateResponse result = exchangeRateService.getBestExchangeRate(validRequest)
            .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertNull(result.bestRate());
        assertNull(result.convertedAmount());
        assertEquals(TestConstants.NO_PROVIDER, result.provider());
        assertEquals(0, result.successfulProviders());
        assertEquals(3, result.totalProviders());

        verify(comparator, times(1)).selectBestRate(eq(validRequest), any(), anyLong());
    }
    
    @Test
    void testGetBestExchangeRate_ProviderThrowsException() {
        // Arrange
        when(api1Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().failure(new RuntimeException("Provider error")));
        when(api2Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(
            ApiResponse.success(TestConstants.XML_PROVIDER, TestConstants.RATE_USD_MXN, TestConstants.CONVERTED_USD_MXN, 150L)
        ));
        when(api3Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(
            ApiResponse.success(TestConstants.ADVANCED_PROVIDER, new BigDecimal("0.84"), new BigDecimal("84.00"), 120L)
        ));

        ExchangeRateResponse expectedResponse = new ExchangeRateResponse(
            TestConstants.RATE_USD_MXN,
            TestConstants.CONVERTED_USD_MXN, 
            TestConstants.XML_PROVIDER, 
            200L, 
            2, 
            3
        );

        when(comparator.selectBestRate(any(), any(), anyLong())).thenReturn(expectedResponse);

        // Act
        ExchangeRateResponse result = exchangeRateService.getBestExchangeRate(validRequest)
            .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(TestConstants.XML_PROVIDER, result.provider());
        assertEquals(TestConstants.RATE_USD_MXN, result.bestRate());
    }
    
    @Test
    void testGetBestExchangeRate_ComparatorThrowsException() {
        // Arrange
        ApiResponse api1Response = ApiResponse.success(
            TestConstants.SIMPLE_PROVIDER, 
            TestConstants.RATE_USD_EUR, 
            TestConstants.CONVERTED_USD_EUR, 
            100L
        );
        
        when(api1Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api1Response));
        when(api2Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api1Response)); // Reuse same response
        when(api3Provider.getExchangeRate(any())).thenReturn(Uni.createFrom().item(api1Response)); // Reuse same response
        
        when(comparator.selectBestRate(any(), any(), anyLong()))
            .thenThrow(new RuntimeException("Comparator error"));

        // Act
        ExchangeRateResponse result = exchangeRateService.getBestExchangeRate(validRequest)
            .await().indefinitely();

        // Assert
        assertNotNull(result);
        assertNull(result.bestRate());
        assertNull(result.convertedAmount());
        assertEquals("ERROR_SERVICIO", result.provider());
        assertEquals(0, result.successfulProviders());
        assertEquals(3, result.totalProviders());
    }
}