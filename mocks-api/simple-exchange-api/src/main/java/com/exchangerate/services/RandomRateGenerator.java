package com.exchangerate.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Generador de tasas aleatorias para Simple Exchange API.
 * Especializado en pares principales con USD.
 * 
 * @author Dev. Domingo J. Ruiz
 */
@ApplicationScoped
public class RandomRateGenerator {
    
    private static final Logger LOG = Logger.getLogger(RandomRateGenerator.class);
    private final Random random = new Random();
    
    @ConfigProperty(name = "exchange.rates.usd.eur.min")
    BigDecimal usdEurMin;
    
    @ConfigProperty(name = "exchange.rates.usd.eur.max") 
    BigDecimal usdEurMax;
    
    @ConfigProperty(name = "exchange.rates.usd.mxn.min")
    BigDecimal usdMxnMin;
    
    @ConfigProperty(name = "exchange.rates.usd.mxn.max")
    BigDecimal usdMxnMax;
    
    @ConfigProperty(name = "exchange.rates.usd.dop.min")
    BigDecimal usdDopMin;
    
    @ConfigProperty(name = "exchange.rates.usd.dop.max")
    BigDecimal usdDopMax;
    
    @ConfigProperty(name = "exchange.rates.eur.mxn.min")
    BigDecimal eurMxnMin;
    
    @ConfigProperty(name = "exchange.rates.eur.mxn.max")
    BigDecimal eurMxnMax;
    
    @ConfigProperty(name = "exchange.rates.eur.dop.min")
    BigDecimal eurDopMin;
    
    @ConfigProperty(name = "exchange.rates.eur.dop.max")
    BigDecimal eurDopMax;
    
    @ConfigProperty(name = "exchange.rates.mxn.dop.min")
    BigDecimal mxnDopMin;
    
    @ConfigProperty(name = "exchange.rates.mxn.dop.max")
    BigDecimal mxnDopMax;
    
    public BigDecimal generateRate(String fromCurrency, String toCurrency) {
        String pair = fromCurrency.toUpperCase() + "_" + toCurrency.toUpperCase();
        
        BigDecimal rate = switch (pair) {
            case "USD_EUR" -> generateRandomInRange(usdEurMin, usdEurMax);
            case "USD_MXN" -> generateRandomInRange(usdMxnMin, usdMxnMax);
            case "USD_DOP" -> generateRandomInRange(usdDopMin, usdDopMax);
            case "EUR_MXN" -> generateRandomInRange(eurMxnMin, eurMxnMax);
            case "EUR_DOP" -> generateRandomInRange(eurDopMin, eurDopMax);
            case "MXN_DOP" -> generateRandomInRange(mxnDopMin, mxnDopMax);
            
            // Pares inversos
            case "EUR_USD" -> BigDecimal.ONE.divide(generateRandomInRange(usdEurMin, usdEurMax), 6, RoundingMode.HALF_UP);
            case "MXN_USD" -> BigDecimal.ONE.divide(generateRandomInRange(usdMxnMin, usdMxnMax), 6, RoundingMode.HALF_UP);
            case "DOP_USD" -> BigDecimal.ONE.divide(generateRandomInRange(usdDopMin, usdDopMax), 6, RoundingMode.HALF_UP);
            case "MXN_EUR" -> BigDecimal.ONE.divide(generateRandomInRange(eurMxnMin, eurMxnMax), 6, RoundingMode.HALF_UP);
            case "DOP_EUR" -> BigDecimal.ONE.divide(generateRandomInRange(eurDopMin, eurDopMax), 6, RoundingMode.HALF_UP);
            case "DOP_MXN" -> BigDecimal.ONE.divide(generateRandomInRange(mxnDopMin, mxnDopMax), 6, RoundingMode.HALF_UP);
            
            default -> {
                LOG.warnf("Par de monedas no configurado: %s", pair);
                yield BigDecimal.ONE;
            }
        };
        
        return rate;
    }
    
    private BigDecimal generateRandomInRange(BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) >= 0) {
            return min;
        }
        
        BigDecimal range = max.subtract(min);
        BigDecimal randomFactor = BigDecimal.valueOf(random.nextDouble());
        BigDecimal randomValue = min.add(range.multiply(randomFactor));
        
        return randomValue.setScale(4, RoundingMode.HALF_UP);
    }
}