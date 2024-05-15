package com.shares.wallet.repo;

import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StocksRepo extends JpaRepository<Stocks, Long> {

    @Query("SELECT s FROM Stocks s WHERE s.user.id = :userId AND s.name = :stockName")
    Optional<Stocks> lookForStock(@Param("stockName") String stockName, @Param("userId") Long userId);

    @Query("SELECT s.quant FROM Stocks s WHERE s.user.id = :userId AND s.name = :stockName")
    Optional<BigDecimal> lookForStockQuant(@Param("stockName") String stockName, @Param("userId") Long userId);
    @Query("SELECT s FROM Stocks s WHERE s.user.id = :userId")
    Optional<List<Stocks>> findByUserId(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE Stocks s SET s.quant = :quant WHERE s.user.id = :userId AND s.name = :stockName")
    int changeQuantityStock(@Param("quant") BigDecimal quant, @Param("userId") Long userId, @Param("stockName") String stockName);

    @Transactional
    @Modifying
    @Query("DELETE Stocks s WHERE s.user.id = :userId AND s.name = :stockName")
    int deleteStock(@Param("userId") Long userId, @Param("stockName") String stockName);


    @Transactional
    default Stocks addStock(Users user, String stockName, BigDecimal quant) {

        Stocks stock = new Stocks(user, quant, stockName);
        save(stock);
        return stock ;
    }
}
