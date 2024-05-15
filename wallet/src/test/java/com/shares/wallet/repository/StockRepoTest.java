package com.shares.wallet.repository;

import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
import com.shares.wallet.repo.StocksRepo;
import com.shares.wallet.repo.UsersRepo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class StockRepoTest {

    @Autowired
    private StocksRepo stocksRepo;

    @Autowired
    private UsersRepo usersRepo;

    private Stocks stockToTest1;

    private Stocks stockToTest2;

    private Stocks stockToTest3;
    private Users user;

    @BeforeEach
    public void init() {
        user = Users.builder()
                .username("userTest")
                .password("Test@1234")
                .build();

        usersRepo.save(user);

        stockToTest1 = Stocks.builder()
                .name("stockTest")
                .quant(BigDecimal.valueOf(2))
                .user(user)
                .build();

        stockToTest2 = Stocks.builder()
                .name("stockTest2")
                .quant(BigDecimal.valueOf(2))
                .user(user)
                .build();

        stockToTest3 = Stocks.builder()
                .name("stockTest3")
                .quant(BigDecimal.valueOf(2))
                .user(user)
                .build();
    }

    @Test
    public void stockRepo_lookForStock_ReturnStocks() {

        //Arrange
        stocksRepo.save(stockToTest1);

        //Act
        Stocks stockTested = stocksRepo.lookForStock(stockToTest1.getName(),
                user.getId()).get();

        //Assert
        Assertions.assertThat(stockTested).isNotNull();
        Assertions.assertThat(stockTested.getName())
                .isEqualTo(stockToTest1.getName());

    }

    @Test
    public void stockRepo_findByUserId_ReturnOPListOfStocks() {

        //Arrange
        stocksRepo.save(stockToTest1);
        stocksRepo.save(stockToTest2);
        stocksRepo.save(stockToTest3);


        //Act
        List<Stocks> stockListTested =
                stocksRepo.findByUserId(user.getId()).get();

        //Assert
        Assertions.assertThat(stockListTested).isNotNull();
        Assertions.assertThat(stockListTested.size())
                .isEqualTo(3);
        Assertions.assertThat(stockListTested.get(0).getName())
                .isEqualTo(stockToTest1.getName());
        Assertions.assertThat(stockListTested.get(1).getName())
                .isEqualTo(stockToTest2.getName());
        Assertions.assertThat(stockListTested.get(2).getName())
                .isEqualTo(stockToTest3.getName());
    }

    @Test
    public void stockRepo_changeQuantityStock_ReturnNofChangedRows() {

        //Arrange
        stocksRepo.save(stockToTest1);
        stocksRepo.save(stockToTest2);
        stocksRepo.save(stockToTest3);

        BigDecimal quantityBeforeChange = stockToTest1.getQuant();
        BigDecimal quantityAfterChange = BigDecimal.valueOf(8);

        //Act
        int changeQuantityResult = stocksRepo.changeQuantityStock(
                quantityAfterChange,
                user.getId(),
                stockToTest1.getName()
        );

        BigDecimal stockAfterChange = stocksRepo.lookForStockQuant(
                stockToTest1.getName(),
                user.getId()
        ).get();

        //Assert
        Assertions.assertThat(changeQuantityResult)
                .isEqualTo(1);
        Assertions.assertThat(stockAfterChange).isNotNull();
        Assertions.assertThat(stockAfterChange)
                .isEqualByComparingTo(quantityAfterChange);

    }

    @Test
    public void stockRepo_deleteStock_ReturnNofChangedRows() {

        //Arrange
        stocksRepo.save(stockToTest1);

        //Act
        int changeQuantityResult = stocksRepo.deleteStock(
                user.getId(),
                stockToTest1.getName()
        );

        Stocks stockDeleted = stocksRepo.lookForStock(
                stockToTest1.getName(),
                user.getId()
        ).orElse(null);

        //Assert
        Assertions.assertThat(changeQuantityResult)
                .isEqualTo(1);
        Assertions.assertThat(stockDeleted).isNull();

    }

    @Test
    public void stockRepo_addStock_ReturnVoid() {

        //Arrange
        String stockName = "StockTest";
        BigDecimal quant = BigDecimal.valueOf(2);

        stocksRepo.addStock(user, stockName, quant);

        //Act
        Stocks stockTested = stocksRepo.lookForStock(stockName,
                user.getId()).get();

        //Assert
        Assertions.assertThat(stockTested).isNotNull();
        Assertions.assertThat(stockTested.getName())
                .isEqualTo(stockName);

    }

}
