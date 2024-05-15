package com.shares.wallet.Proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Qualifier("StockAPI")
public class StockProxy implements StockProxyInterface {

    private final RestTemplate restTemplate;

    public StockProxy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public StockQuote getStockQuote(String symbol) throws JsonProcessingException {
        // Make the API call
        String apiKey = System.getenv("API_KEY");

        String apiUrl = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                + symbol + "&apikey=" + apiKey;

        GlobalQuote response = restTemplate.getForObject(apiUrl, GlobalQuote.class);

        if (response == null) {
            throw new IllegalArgumentException();
        }

        StockQuote stock = response.getStockQuote();

        if (stock == null) {
            throw new IllegalArgumentException();
        }

        return stock;
    }
}