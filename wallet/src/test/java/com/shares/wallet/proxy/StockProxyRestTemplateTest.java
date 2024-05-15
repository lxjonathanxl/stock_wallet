package com.shares.wallet.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.Proxy.StockProxy;
import com.shares.wallet.model.GlobalQuote;
import com.shares.wallet.model.StockQuote;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.Matchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StockProxyRestTemplateTest {

    private StockProxy proxy;
    private RestTemplate restTemplate;

    @BeforeEach
    public void init() {
        restTemplate = new RestTemplate();
        proxy = new StockProxy(restTemplate);
    }

    @Test
    public void stockProxy_getStockQuote_ReturnStockQuote_success() throws JsonProcessingException {
        //Arrange
        String symbol = "NFLX";
        //Act
        StockQuote result = proxy.getStockQuote(symbol);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSymbol())
                .isEqualTo(symbol);
    }

    @Test
    public void stockProxy_getStockQuote_ReturnStockQuote_fail_InvalidStock() throws JsonProcessingException {
        //Arrange
        String symbol = "TestInvalidStockSymbol";

        //act
        StockQuote result = proxy.getStockQuote(symbol);

        //Assert
        Assertions.assertThat(result.getSymbol()).isNull();
    }

    @Test
    public void stockProxy_getStockQuote_ReturnStockQuote_fail_restTemplateReturnNull() {
        //Arrange
        String symbol = "AAPL";

        RestTemplate restTemplateError = mock(RestTemplate.class);
        when(restTemplateError.getForObject(anyString(), ArgumentMatchers.any()))
                .thenReturn(null);

        StockProxy proxyError = new StockProxy(restTemplateError);

        //act and Assert
        assertThatThrownBy(() -> proxyError.getStockQuote(symbol))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void stockProxy_getStockQuote_ReturnStockQuote_fail_restTemplateReturnResponseWithNullStockQuote() {
        //Arrange
        String symbol = "AAPL";

        GlobalQuote response = mock(GlobalQuote.class);
        when(response.getStockQuote()).thenReturn(null);

        RestTemplate restTemplateError = mock(RestTemplate.class);
        when(restTemplateError.getForObject(anyString(), ArgumentMatchers.any()))
                .thenReturn(response);

        StockProxy proxyError = new StockProxy(restTemplateError);

        //act and Assert
        assertThatThrownBy(() -> proxyError.getStockQuote(symbol))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
