package com.shares.wallet.Proxy;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Qualifier("StockMockAPI")
public class StockProxyMockAPI implements StockProxyInterface {

    private final static Logger stockProxyMockApi = LoggerFactory.getLogger(StockProxyMockAPI.class);

    private final RestTemplate restTemplate;
    private String apiUrl;

    public StockProxyMockAPI(RestTemplate restTemplate,
                             @Value("${wallet.stockProxyMockApi.url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    @Override
    public StockQuote getStockQuote(String symbol) throws JsonProcessingException {
        // Make the API call
        apiUrl = apiUrl + symbol;

        try {
            StockQuote stock = restTemplate.getForObject(apiUrl, StockQuote.class);

            if (stock == null) {
                stockProxyMockApi.error("Error getting response from mock stock api, symbol: {}, apiUrl: {}",
                        symbol, apiUrl);
                throw new IllegalArgumentException("Invalid Symbol");
            }

            return stock;

        } catch (HttpClientErrorException.BadRequest ex) {
            stockProxyMockApi.error("Error stock not found through api, stock: {}",
                    symbol);
            throw new IllegalArgumentException("Stock not found in mock database");
        }


    }
}
