package com.shares.wallet.services;

import com.shares.wallet.dto.TransactionRequest;
import com.shares.wallet.exceptions.AlterUserStockException;
import com.shares.wallet.exceptions.UpdateCashException;
import com.shares.wallet.exceptions.UserNotFoundException;
import com.shares.wallet.exceptions.UsernameTakenException;
import com.shares.wallet.model.History;
import com.shares.wallet.model.Stocks;
import com.shares.wallet.model.Users;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private UsersService usersService;
    @Mock
    private StockService stockService;
    @Mock
    private HistoryService historyService;

    @InjectMocks
    private TransactionService transactionService;

    private Users userTest;

    @BeforeEach
    public void init() {
        userTest = Users.builder()
                .username("UserTest")
                .password("Test@1234")
                .build();
    }

    @Test
    public void TransactionService_buy_ReturnString_success_UserHasStock() {
        //Arrange

        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        History historyTest = History.builder()
                .name("Test")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        String action = "buy";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.subtract(total);


        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.updateCash(username, userCash))
                .thenReturn(1);
        when(usersService.findUser(username))
                .thenReturn(userTest);
        when(stockService.hasStock(userTest, request.getSymbol()))
                .thenReturn(true);
        when(stockService.updateStockBuy(
                userTest, quant, request.getSymbol()))
                .thenReturn(1);
        when(historyService.InsertHistory(
                userTest, action, quant, request))
                .thenReturn(historyTest);

        //Act
        String result = transactionService.buy(request, username);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result)
                .isEqualTo("Stock added to portfolio");
    }

    @Test
    public void TransactionService_buy_ReturnString_success_UserDontHaveStock() {
        //Arrange

        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        History historyTest = History.builder()
                .name("Test")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        String action = "buy";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.subtract(total);

        Stocks stockTest = Stocks
                .builder()
                .user(userTest)
                .quant(quant)
                .name(request.getSymbol())
                .build();


        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.updateCash(username, userCash))
                .thenReturn(1);
        when(usersService.findUser(username))
                .thenReturn(userTest);
        when(stockService.hasStock(userTest, request.getSymbol()))
                .thenReturn(false);
        when(stockService.insertStock(
                userTest, quant, request.getSymbol()))
                .thenReturn(stockTest);
        when(historyService.InsertHistory(
                userTest, action, quant, request))
                .thenReturn(historyTest);

        //Act
        String result = transactionService.buy(request, username);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result)
                .isEqualTo("Stock added to portfolio");
    }

    @Test
    public void TransactionService_buy_ReturnString_fail_notEnoughMoney() {
        //Arrange
        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        BigDecimal userWallet = BigDecimal.valueOf(20);

        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        //Act
        String result = transactionService.buy(request, username);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result)
                .isEqualTo("Insufficient money to buy " + request.getShares()
                        + " shares of " + request.getSymbol()
                        + " for: $" + total);
    }

    @Test
    public void TransactionService_buy_ThrowException_fail_UsernameNotFound() {
        //Arrange

        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.subtract(total);

        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.updateCash(username, userCash))
                .thenReturn(1);
        when(usersService.findUser(username))
                .thenThrow(UsernameNotFoundException.class);

        //Act and Assert
        assertThatThrownBy(
                () -> transactionService.buy(request, username))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found in database");
    }

    @Test
    public void TransactionService_buy_ThrowException_fail_DataError() {
        //Arrange
        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.subtract(total);

        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.updateCash(username, userCash))
                .thenReturn(1);
        when(usersService.findUser(username))
                .thenThrow(new DataAccessException("dataError") {
                });

        //Act and Assert
        assertThatThrownBy(
                () -> transactionService.buy(request, username))
                .isInstanceOf(AlterUserStockException.class)
                .hasMessageContaining("Error changing user stocks in database");
    }

    @Test
    public void transactionService_sell_returnString_success_userHasMoreStock() {
        //Arrange
        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        History historyTest = History.builder()
                .name("Test")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        String action = "sell";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.add(total);

        Stocks stockTest = Stocks.builder()
                .name("Test")
                .quant(BigDecimal.valueOf(10))
                .build();

        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.findUser(username))
                .thenReturn(userTest);
        when(stockService.hasStock(userTest, request.getSymbol()))
                .thenReturn(true);
        when(stockService.findStock(userTest, request.getSymbol()))
                .thenReturn(stockTest);
        when(stockService.updateStockSell(userTest, quant, request.getSymbol()))
                .thenReturn(1);
        when(historyService.InsertHistory(userTest, action, quant, request))
                .thenReturn(historyTest);

        //Act
        String result = transactionService.sell(request, username);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo("Stock sold");

    }

    @Test
    public void transactionService_sell_returnString_success_userHasExactQuantOfStock() {
        //Arrange
        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        History historyTest = History.builder()
                .name("Test")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        String action = "sell";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.add(total);

        Stocks stockTest = Stocks.builder()
                .name("Test")
                .quant(BigDecimal.valueOf(2))
                .build();

        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.updateCash(username, userCash))
                .thenReturn(1);
        when(usersService.findUser(username))
                .thenReturn(userTest);
        when(stockService.hasStock(userTest, request.getSymbol()))
                .thenReturn(true);
        when(stockService.findStock(userTest, request.getSymbol()))
                .thenReturn(stockTest);
        when(stockService.deleteStock(userTest, request.getSymbol()))
                .thenReturn(1);
        when(historyService.InsertHistory(userTest, action, quant, request))
                .thenReturn(historyTest);

        //Act
        String result = transactionService.sell(request, username);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo("Stock sold");

    }

    @Test
    public void transactionService_sell_throwException_fail_DataErrorUpdateCash() {
        //Arrange
        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.add(total);

        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.updateCash(username, userCash))
                .thenThrow(new DataAccessException("DataError") {
                });

        //Act and Assert
        assertThatThrownBy(() -> transactionService.sell(request, username))
                .isInstanceOf(UpdateCashException.class)
                .hasMessageContaining("server error: unable to update cash");

    }

    @Test
    public void transactionService_sell_throwException_fail_UserNotFound() {
        //Arrange
        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.add(total);

        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.updateCash(username, userCash))
                .thenReturn(1);
        when(usersService.findUser(username))
                .thenThrow(UsernameNotFoundException.class);

        //Act and Assert
        assertThatThrownBy(() -> transactionService.sell(request, username))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found in database");

    }

    @Test
    public void transactionService_sell_throwException_fail_findUserDataError() {
        //Arrange
        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.add(total);

        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.updateCash(username, userCash))
                .thenReturn(1);
        when(usersService.findUser(username))
                .thenThrow(new DataAccessException("DataError") {
                });

        //Act and Assert
        assertThatThrownBy(() -> transactionService.sell(request, username))
                .isInstanceOf(AlterUserStockException.class)
                .hasMessageContaining("Error changing user stocks in database");

    }

    @Test
    public void transactionService_sell_throwException_fail_UserDontHaveStock() {
        //Arrange
        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.add(total);

        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.updateCash(username, userCash))
                .thenReturn(1);
        when(usersService.findUser(username))
                .thenReturn(userTest);
        when(stockService.hasStock(userTest, request.getSymbol()))
                .thenReturn(false);

        //Act and Assert
        assertThatThrownBy(() -> transactionService.sell(request, username))
                .isInstanceOf(AlterUserStockException.class)
                .hasMessageContaining("User doesn't have shares of "
                        + request.getSymbol());

    }

    @Test
    public void transactionService_sell_throwException_fail_UserDontHaveEnoughStock() {
        //Arrange
        TransactionRequest request = TransactionRequest
                .builder()
                .price(BigDecimal.valueOf(42))
                .shares(BigDecimal.valueOf(2))
                .symbol("TEST")
                .build();

        BigDecimal quant = request.getShares();
        BigDecimal total = request.getPrice().multiply(quant);
        String username = "UsernameTest";
        BigDecimal userWallet = BigDecimal.valueOf(100);
        BigDecimal userCash = userWallet.add(total);

        Stocks stockTest = Stocks.builder()
                .name("test")
                .quant(BigDecimal.valueOf(1))
                .build();

        when(usersService.lookIntoCash(username))
                .thenReturn(userWallet);
        when(usersService.updateCash(username, userCash))
                .thenReturn(1);
        when(usersService.findUser(username))
                .thenReturn(userTest);
        when(stockService.hasStock(userTest, request.getSymbol()))
                .thenReturn(true);
        when(stockService.findStock(userTest, request.getSymbol()))
                .thenReturn(stockTest);

        //Act and Assert
        assertThatThrownBy(() -> transactionService.sell(request, username))
                .isInstanceOf(AlterUserStockException.class)
                .hasMessageContaining("User doesn't have " + quant + " shares of "
                        + request.getSymbol());

    }
}
