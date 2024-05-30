package com.shares.wallet.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shares.wallet.Proxy.StockProxy;
import com.shares.wallet.Proxy.StockProxyInterface;
import com.shares.wallet.dto.QuoteRequest;
import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.exceptions.*;
import com.shares.wallet.model.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.origin.TextResourceOrigin;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final StockProxyInterface stockProxy;
    private final UsersService usersService;
    private final StockService stockService;
    private final HistoryService historyService;

    public TransactionService(@Qualifier("StockMockAPI") StockProxyInterface stockProxy, UsersService usersService, StockService stockService, HistoryService historyService) {
        this.stockProxy = stockProxy;
        this.usersService = usersService;
        this.stockService = stockService;
        this.historyService = historyService;
    }

    public StockQuote lookUpStock(QuoteRequest quoteRequest) throws JsonProcessingException, IllegalArgumentException {

        String symbol = quoteRequest.getSymbol().toUpperCase();

        return stockProxy.getStockQuote(symbol);
    }

    public List<Stocks> findUserStocks(String username) throws UsernameNotFoundException {

        Users user = usersService.findUser(username);
        return stockService.getUserStocks(user);
    }

    @Transactional
    public String buy(TransactionRequest transactionRequest, String username) {

        BigDecimal quant = transactionRequest.getShares();
        BigDecimal total = transactionRequest.getPrice().multiply(quant);
        BigDecimal userCash = BigDecimal.valueOf(0);

        try {
            userCash = usersService.lookIntoCash(username);
        } catch (DataAccessException | ServerErrorException dataError) {
            //TODO logging
            throw new UpdateCashException("server error: unable to access cash");
        }

        userCash = userCash.subtract(total);
        if (userCash.signum() < 0) {
            return "Insufficient money to buy " + transactionRequest.getShares()
                    + " shares of " + transactionRequest.getSymbol()
                    + " for: $" + total;
        }

        try {
            usersService.updateCash(username, userCash);
        } catch (DataAccessException | ServerErrorException dataError) {
            //TODO logging
            throw new UpdateCashException("server error: unable to update cash");
        }

        try {
            Users user = usersService.findUser(username);
            String action = "buy";

            if (stockService.hasStock(user, transactionRequest.getSymbol())) {
                stockService.updateStockBuy(user, quant, transactionRequest.getSymbol());
            } else {
                stockService.insertStock(user, quant, transactionRequest.getSymbol());
            }

            historyService.InsertHistory(user, action, quant, transactionRequest);
            return "Stock added to portfolio";

        } catch (UsernameNotFoundException userError) {
            //TODO logging
            throw new UserNotFoundException("User not found in database");

        } catch (DataAccessException | ServerErrorException dataError) {
            //TODO logging
            throw new AlterUserStockException("Error changing user stocks in database");

        }

    }

    @Transactional
    public String sell(TransactionRequest transactionRequest, String username) {

        BigDecimal quantRequested = transactionRequest.getShares();
        BigDecimal total = transactionRequest.getPrice().multiply(quantRequested);
        BigDecimal userCash = BigDecimal.valueOf(0);

        try {
            userCash = usersService.lookIntoCash(username);
        } catch (DataAccessException | ServerErrorException dataError) {
            //TODO logging
            throw new UpdateCashException("server error: unable to access cash");
        }
        userCash = userCash.add(total);

        try {
            usersService.updateCash(username, userCash);
        } catch (DataAccessException | ServerErrorException dataError) {
            //TODO logging
            throw new UpdateCashException("server error: unable to update cash");
        }

        try {
            Users user = usersService.findUser(username);
            String action = "sell";

            if (stockService.hasStock(user, transactionRequest.getSymbol())) {

                Stocks UserStock = stockService.findStock(user, transactionRequest.getSymbol());
                BigDecimal checkedQuant = UserStock.getQuant().subtract(quantRequested);

                if (checkedQuant.compareTo(BigDecimal.ZERO) > 0) {

                    stockService.updateStockSell(user, quantRequested, transactionRequest.getSymbol());
                    
                } else if (checkedQuant.compareTo(BigDecimal.ZERO) == 0) {
                    stockService.deleteStock(user, transactionRequest.getSymbol());
                } else {
                    throw new AlterUserStockException("User doesn't have " + quantRequested + " shares of "
                            + transactionRequest.getSymbol());
                }

            } else {
                throw new AlterUserStockException("User doesn't have shares of "
                        + transactionRequest.getSymbol());
            }

            historyService.InsertHistory(user, action, quantRequested, transactionRequest);
            return "Stock sold";
        } catch (UsernameNotFoundException userError) {
            //TODO logging
            throw new UserNotFoundException("User not found in database");

        } catch (DataAccessException | ServerErrorException dataError) {
            //TODO logging
            throw new AlterUserStockException("Error changing user stocks in database");
        }
    }

    public List<StockDisplay> displayStocks(String user) {

        List<StockDisplay> stockDisplays = new ArrayList<>();

        try {
            List<Stocks> stocksDb = findUserStocks(user);

            for (Stocks stock : stocksDb) {

                String symbol = stock.getName();
                StockQuote quote = stockProxy.getStockQuote(symbol);
                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(quote.getPrice()));
                BigDecimal total = stock.getQuant().multiply(price);
                stockDisplays.add(new StockDisplay(symbol, stock.getQuant(),
                        price, total));
            }
        } catch (JsonProcessingException | IllegalArgumentException serverError) {
            //todo logging
            throw new ServerErrorException("Error looking for stock");
        } catch (UsernameNotFoundException userError) {
            //todo logging
            throw new ServerErrorException("Error looking for user on database");
        }

        return stockDisplays;
    }

    public List<History> displayUserHistory(String username) {
        try {
            Users user = usersService.findUser(username);
            return  historyService.FindHistory(user);
        } catch (UsernameNotFoundException userError) {
            //TODO logging
            throw new HistoryNotFoundException(
                    "Error looking for user history, username not found in database");
        } catch (RuntimeException historyError) {
            //TODO logging
            throw new HistoryNotFoundException(
                    "Error looking for user history, history not found in database");
        }
    }
}
