package com.shares.wallet.repo;

import com.shares.wallet.model.History;
import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HistoryRepo extends JpaRepository<History, Long> {

    @Query("SELECT s FROM History s WHERE s.user.id = :userId")
    Optional<List<History>> findByUserId(@Param("userId") Long userId);

    @Transactional
    default History addHistory(Users user, String name, BigDecimal quant, BigDecimal price, String action) {

        History history = new History(user, action, quant, name, price);
        save(history);
        return history;
    }
}
