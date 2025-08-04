package com.exchangerate.services.contracts;

import com.exchangerate.models.request.ExchangeRateRequest;
import com.exchangerate.models.response.ExchangeRateResponse;

import io.smallrye.mutiny.Uni;

public interface IExchangeRateService { 
    Uni<ExchangeRateResponse> getBestExchangeRate(ExchangeRateRequest request);
}