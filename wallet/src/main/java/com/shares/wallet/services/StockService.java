package com.shares.wallet.services;

import com.shares.wallet.Proxy.StockProxy;
import com.shares.wallet.exceptions.ServerErrorException;
import com.shares.wallet.model.StockDisplay;
import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
import com.shares.wallet.repo.StocksRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StockService {

    private final static Logger stockServiceLogger = LoggerFactory.getLogger(StockService.class);
    private final StocksRepo stocksRepo;

    public StockService(StocksRepo stocksRepo) {
        this.stocksRepo = stocksRepo;
    }

    public Boolean hasStock(Users user, String stockName) {
        return stocksRepo.lookForStock(stockName, user.getId()).isPresent();
    }

    public Stocks findStock(Users user, String stockName) {
        return stocksRepo.lookForStock(stockName, user.getId())
                .orElseThrow(() ->
                        new ServerErrorException("Stock not found"));
    }

    public int updateStockBuy(Users user, BigDecimal quant, String stockName) {
        stockServiceLogger.info("Trying to update stock, action of buying stock," +
                "stock: {}, user: {}", stockName, user.getUsername());

        Stocks stock = stocksRepo.lookForStock(stockName, user.getId())
                .orElseThrow(() ->
                        new ServerErrorException("Stock not found"));

        quant = stock.getQuant().add(quant);

        return stocksRepo.changeQuantityStock(quant, user.getId(), stockName);
    }

    public int updateStockSell(Users user, BigDecimal quant, String stockName) {
        stockServiceLogger.info("Trying to update stock, action of selling stock, " +
                "stock: {}, user: {}", stockName, user.getUsername());

        Stocks stock = stocksRepo.lookForStock(stockName, user.getId())
                .orElseThrow(() ->
                        new ServerErrorException("Stock not found"));

        quant = stock.getQuant().subtract(quant);

        return stocksRepo.changeQuantityStock(quant, user.getId(), stockName);
    }

    public List<Stocks> getUserStocks (Users user) {
        return stocksRepo.findByUserId(user.getId()).get();
    }

    public Stocks insertStock(Users user, BigDecimal quant, String stockName) {
        stockServiceLogger.info("Trying to insert stock, stock: {}, user: {}"
        ,stockName, user.getUsername());
        return stocksRepo.addStock(user, stockName, quant);
    }

    public int deleteStock(Users user, String stockName) {
        stockServiceLogger.info("Trying to delete stock, stock: {}, user: {}"
                ,stockName, user.getUsername());
        return stocksRepo.deleteStock(user.getId(), stockName);
    }

}
