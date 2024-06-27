package com.shares.wallet.services;

import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.exceptions.HistoryNotFoundException;
import com.shares.wallet.model.History;
import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
import com.shares.wallet.repo.HistoryRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HistoryService {

    private final HistoryRepo historyRepo;
    private final NotificationService notificationService;

    private final static Logger historyServiceLogger = LoggerFactory.getLogger(HistoryService.class);

    public HistoryService(HistoryRepo historyRepo, NotificationService notificationService) {
        this.historyRepo = historyRepo;
        this.notificationService = notificationService;
    }

    public History InsertHistory(Users user, String action, BigDecimal quant, TransactionRequest request) {

        historyServiceLogger.info("trying to insert history in database, user: {}, action: {}"
                , user.getUsername(), action);
        History resultHistory = historyRepo.addHistory(user, request.getSymbol(), quant, request.getPrice(), action);

        notificationService.notify(resultHistory);

        return resultHistory;
    }

    public List<History> FindHistory(Users user) {
        return historyRepo.findByUserId(user.getId())
                .orElseThrow(() -> new HistoryNotFoundException());
    }
}
