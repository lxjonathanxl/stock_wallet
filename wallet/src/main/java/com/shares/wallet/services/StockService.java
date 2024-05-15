package com.shares.wallet.services;

import com.shares.wallet.Proxy.StockProxy;
import com.shares.wallet.model.StockDisplay;
import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
import com.shares.wallet.repo.StocksRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StockService {

    private final StocksRepo stocksRepo;

    public StockService(StocksRepo stocksRepo) {
        this.stocksRepo = stocksRepo;
    }

    public Boolean hasStock(Users user, String stockName) {
        return stocksRepo.lookForStock(stockName, user.getId()).isPresent();
    }

    public Stocks findStock(Users user, String stockName) {
        return stocksRepo.lookForStock(stockName, user.getId()).get();
    }

    public int updateStockBuy(Users user, BigDecimal quant, String stockName) {
        Stocks stock = stocksRepo.lookForStock(stockName, user.getId()).get();

        quant = stock.getQuant().add(quant);

        return stocksRepo.changeQuantityStock(quant, user.getId(), stockName);
    }

    public int updateStockSell(Users user, BigDecimal quant, String stockName) {
        Stocks stock = stocksRepo.lookForStock(stockName, user.getId()).get();

        quant = stock.getQuant().subtract(quant);

        return stocksRepo.changeQuantityStock(quant, user.getId(), stockName);
    }

    public List<Stocks> getUserStocks (Users user) {
        return stocksRepo.findByUserId(user.getId()).get();
    }

    public Stocks insertStock(Users user, BigDecimal quant, String stockName) {
       return stocksRepo.addStock(user, stockName, quant);
    }

    public int deleteStock(Users user, String stockName) {
        return stocksRepo.deleteStock(user.getId(), stockName);
    }

}
