package com.exchangerate.services.contracts;

import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ApiResponse;

import io.smallrye.mutiny.Uni;

public interface IExchangeRateProvider {
    String getProviderName();
    Uni<ApiResponse> getExchangeRate(ExchangeRateRequest request);
}
