package com.shares.wallet.repository;


import com.shares.wallet.model.History;
import com.shares.wallet.model.Users;
import com.shares.wallet.repo.HistoryRepo;
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
public class HistoryRepoTest {

    @Autowired
    HistoryRepo historyRepo;
    @Autowired
    UsersRepo usersRepo;

    private Users user;

    private History history1;
    private History history2;
    private History history3;

    @BeforeEach
    public void init() {
        user = Users.builder()
                .username("userTest")
                .password("Test@1234")
                .build();

        usersRepo.save(user);

        history1 = History.builder()
                .price(BigDecimal.valueOf(10))
                .action("buy")
                .quant(BigDecimal.valueOf(10))
                .user(user)
                .name("HistoryTest1")
                .build();

        history2 = History.builder()
                .price(BigDecimal.valueOf(10))
                .action("buy")
                .quant(BigDecimal.valueOf(10))
                .user(user)
                .name("HistoryTest2")
                .build();

        history3 = History.builder()
                .price(BigDecimal.valueOf(10))
                .action("buy")
                .quant(BigDecimal.valueOf(10))
                .user(user)
                .name("HistoryTest3")
                .build();

    }

    @Test
    public void historyRepo_findByUserId_ReturnListOfHistory() {

        //Arrange
        historyRepo.save(history1);
        historyRepo.save(history2);
        historyRepo.save(history3);

        //Act
        List<History> historyListTested =
                historyRepo.findByUserId(user.getId()).get();

        //Assert
        Assertions.assertThat(historyListTested).isNotNull();
        Assertions.assertThat(historyListTested.size())
                .isEqualTo(3);
        Assertions.assertThat(historyListTested.get(0).getName())
                .isEqualTo(history1.getName());
        Assertions.assertThat(historyListTested.get(1).getName())
                .isEqualTo(history2.getName());
        Assertions.assertThat(historyListTested.get(2).getName())
                .isEqualTo(history3.getName());
    }

    @Test
    public void historyRepo_addHistory_ReturnVoid() {
        //Arrange
        String name = "HistoryTest";
        String action = "buy";
        BigDecimal quant = BigDecimal.valueOf(10);
        BigDecimal price = BigDecimal.valueOf(10);

        //Act
        historyRepo.addHistory(user, name, quant, price, action);

        List<History> historyListTested =
                historyRepo.findByUserId(user.getId()).get();

        //Assert
        Assertions.assertThat(historyListTested).isNotNull();
        Assertions.assertThat(historyListTested.get(0).getName())
                .isEqualTo(name);
    }
}
