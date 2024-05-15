package com.shares.wallet.Proxy;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Qualifier("StockMockAPI")
public class StockProxyMockAPI implements StockProxyInterface {

    private final RestTemplate restTemplate;

    public StockProxyMockAPI(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public StockQuote getStockQuote(String symbol) throws JsonProcessingException {
        // Make the API call
        String apiUrl = "http://mock-stock-data-server:8090/get/" + symbol;

        try {
            StockQuote stock = restTemplate.getForObject(apiUrl, StockQuote.class);

            if (stock == null) {
                throw new IllegalArgumentException("Invalid Symbol");
            }

            return stock;

        } catch (HttpClientErrorException.BadRequest ex) {
            throw new IllegalArgumentException("Stock not found in mock database");
        }


    }
}
