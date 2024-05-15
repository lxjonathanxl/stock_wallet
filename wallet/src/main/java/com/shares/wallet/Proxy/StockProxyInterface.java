package com.shares.wallet.Proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.model.StockQuote;

public interface StockProxyInterface {

    public StockQuote getStockQuote(String symbol) throws JsonProcessingException;
}
