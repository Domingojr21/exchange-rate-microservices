package com.exchangerate.services;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ApiResponse;
import com.exchangerate.models.response.ExchangeRateResponse;
import com.exchangerate.services.implementations.ExchangeRateComparator;
import com.exchangerate.utils.TestConstants;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ExchangeRateComparatorTest {

    private ExchangeRateComparator comparator;
    private ExchangeRateRequest request;

    @BeforeEach
    void setUp() {
        comparator = new ExchangeRateComparator();
        request = new ExchangeRateRequest(
            TestConstants.USD, 
            TestConstants.EUR, 
            TestConstants.AMOUNT_100
        );
    }

    @Test
    void testSelectBestRate_Success() {
        // Arrange
        List<ApiResponse> responses = List.of(
            ApiResponse.success("API1", new BigDecimal("0.85"), new BigDecimal("85.00"), 100L),
            ApiResponse.success("API2", new BigDecimal("0.86"), new BigDecimal("86.00"), 150L),
            ApiResponse.success("API3", new BigDecimal("0.84"), new BigDecimal("84.00"), 120L)
        );

        // Act
        ExchangeRateResponse result = comparator.selectBestRate(request, responses, 200L);

        // Assert
        assertNotNull(result);
        assertEquals("API2", result.provider());
        assertEquals(new BigDecimal("0.86"), result.bestRate());
        assertEquals(new BigDecimal("86.00"), result.convertedAmount());
        assertEquals(3, result.successfulProviders());
        assertEquals(3, result.totalProviders());
        assertEquals(200L, result.responseTimeMs());
    }

    @Test
    void testSelectBestRate_NoSuccessfulResponses() {
        // Arrange
        List<ApiResponse> responses = List.of(
            ApiResponse.failure("API1", "Connection timeout", 5000L),
            ApiResponse.failure("API2", "Invalid response", 3000L),
            ApiResponse.failure("API3", "Service unavailable", 1000L)
        );

        // Act
        ExchangeRateResponse result = comparator.selectBestRate(request, responses, 5000L);

        // Assert
        assertNotNull(result);
        assertEquals(TestConstants.NO_PROVIDER, result.provider());
        assertNull(result.bestRate());
        assertNull(result.convertedAmount());
        assertEquals(0, result.successfulProviders());
        assertEquals(3, result.totalProviders());
    }

    @Test
    void testSelectBestRate_MixedResponses() {
        // Arrange
        List<ApiResponse> responses = List.of(
            ApiResponse.success("API1", new BigDecimal("0.85"), new BigDecimal("85.00"), 100L),
            ApiResponse.failure("API2", "Connection timeout", 5000L),
            ApiResponse.success("API3", new BigDecimal("0.87"), new BigDecimal("87.00"), 120L)
        );

        // Act
        ExchangeRateResponse result = comparator.selectBestRate(request, responses, 200L);

        // Assert
        assertNotNull(result);
        assertEquals("API3", result.provider());
        assertEquals(new BigDecimal("0.87"), result.bestRate());
        assertEquals(new BigDecimal("87.00"), result.convertedAmount());
        assertEquals(2, result.successfulProviders());
        assertEquals(3, result.totalProviders());
    }
    
    @Test
    void testSelectBestRate_EmptyResponsesList() {
        // Arrange
        List<ApiResponse> responses = new ArrayList<>();

        // Act
        ExchangeRateResponse result = comparator.selectBestRate(request, responses, 100L);

        // Assert
        assertNotNull(result);
        assertEquals(TestConstants.NO_PROVIDER, result.provider());
        assertNull(result.bestRate());
        assertNull(result.convertedAmount());
        assertEquals(0, result.successfulProviders());
        assertEquals(0, result.totalProviders());
    }
    
    @Test
    void testSelectBestRate_OneSuccessfulResponse() {
        // Arrange
        List<ApiResponse> responses = List.of(
            ApiResponse.success("API1", new BigDecimal("0.85"), new BigDecimal("85.00"), 100L)
        );

        // Act
        ExchangeRateResponse result = comparator.selectBestRate(request, responses, 100L);

        // Assert
        assertNotNull(result);
        assertEquals("API1", result.provider());
        assertEquals(new BigDecimal("0.85"), result.bestRate());
        assertEquals(new BigDecimal("85.00"), result.convertedAmount());
        assertEquals(1, result.successfulProviders());
        assertEquals(1, result.totalProviders());
    }
    
    @Test
    void testSelectBestRate_TieInConvertedAmount() {
        // Arrange - two providers with the same converted amount
        List<ApiResponse> responses = List.of(
            ApiResponse.success("API1", new BigDecimal("0.85"), new BigDecimal("85.00"), 100L),
            ApiResponse.success("API2", new BigDecimal("0.85"), new BigDecimal("85.00"), 150L)
        );

        // Act
        ExchangeRateResponse result = comparator.selectBestRate(request, responses, 200L);

        // Assert - should select the first one due to max comparison
        assertNotNull(result);
        assertEquals("API1", result.provider());
        assertEquals(new BigDecimal("0.85"), result.bestRate());
        assertEquals(new BigDecimal("85.00"), result.convertedAmount());
        assertEquals(2, result.successfulProviders());
        assertEquals(2, result.totalProviders());
    }
}