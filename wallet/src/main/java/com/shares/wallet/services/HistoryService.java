package com.shares.wallet.services;

import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.exceptions.HistoryNotFoundException;
import com.shares.wallet.model.History;
import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
import com.shares.wallet.repo.HistoryRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HistoryService {

    private final HistoryRepo historyRepo;

    public HistoryService(HistoryRepo historyRepo) {
        this.historyRepo = historyRepo;
    }

    public History InsertHistory(Users user, String action, BigDecimal quant, TransactionRequest request) {

    return historyRepo.addHistory(user, request.getSymbol(), quant, request.getPrice(), action);

    }

    public List<History> FindHistory(Users user) {
        return historyRepo.findByUserId(user.getId())
                .orElseThrow(() -> new HistoryNotFoundException());
    }
}
