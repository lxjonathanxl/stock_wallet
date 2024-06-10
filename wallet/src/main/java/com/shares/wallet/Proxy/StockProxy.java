package com.shares.wallet.Proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Qualifier("StockAPI")
public class StockProxy implements StockProxyInterface {

    private final static Logger stockProxyLogger = LoggerFactory.getLogger(StockProxy.class);

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
            stockProxyLogger.error("Error getting response from stock api, symbol: {}, apiUrl: {}",
                    symbol, apiUrl);
            throw new IllegalArgumentException();
        }

        StockQuote stock = response.getStockQuote();

        if (stock == null) {
            stockProxyLogger.error("Error stock not found through api, stock: {}",
                    symbol);
            throw new IllegalArgumentException();
        }

        return stock;
    }
}