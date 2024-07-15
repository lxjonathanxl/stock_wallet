package com.shares.wallet.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.Proxy.StockProxy;
import com.shares.wallet.Proxy.StockProxyMockAPI;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StockProxyMockAPIRestTemplateTest {

    private StockProxyMockAPI proxy;
    private RestTemplate restTemplate;
    private String apiUrl = "http://localhost:8090/get/";

    @BeforeEach
    public void init() {
        restTemplate = new RestTemplate();
        proxy = new StockProxyMockAPI(restTemplate, apiUrl);
    }

    @Test
    public void stockProxy_getStockQuote_ReturnStockQuote_fail_InvalidStock() throws JsonProcessingException {
        //Arrange
        String symbol = "InvalidStockSymbol";

        //Act and Assert
        assertThatThrownBy(() -> proxy.getStockQuote(symbol))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void stockProxy_getStockQuote_ReturnStockQuote_fail_restTemplateReturnNull() {
        //Arrange
        String symbol = "AAPL";

        RestTemplate restTemplateError = mock(RestTemplate.class);
        when(restTemplateError.getForObject(anyString(), ArgumentMatchers.any()))
                .thenReturn(null);

        StockProxyMockAPI proxyError = new StockProxyMockAPI(restTemplateError, apiUrl);

        //act and Assert
        assertThatThrownBy(() -> proxyError.getStockQuote(symbol))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
