package com.exchangerate.services.contracts;

import java.util.List;

import com.exchangerate.models.response.ApiResponse;
import com.exchangerate.models.response.ExchangeRateResponse;
import com.exchangerate.models.request.ExchangeRateRequest;

public interface IExchangeRateComparator {
    ExchangeRateResponse selectBestRate(ExchangeRateRequest request, List<ApiResponse> responses, long totalResponseTime);
}