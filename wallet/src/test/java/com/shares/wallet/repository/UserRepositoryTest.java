package com.shares.wallet.repository;

import com.shares.wallet.model.Users;
import com.shares.wallet.repo.UsersRepo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepositoryTest {

    @Autowired
    private UsersRepo usersRepo;

    @Test
    public void userRepository_FindUserCash_ReturnBigDecimal() {

        //Arrange
        BigDecimal userCash = BigDecimal.valueOf(100);
        Users user = Users.builder()
                .username("userTest")
                .password("userTest")
                .cash(userCash)
                .build();

        usersRepo.save(user);

        //Act
        BigDecimal userTestCash = usersRepo.findUserCash(
                user.getUsername());

        //Assert
        Assertions.assertThat(userTestCash).isNotNull();
        Assertions.assertThat(userTestCash)
                .isEqualByComparingTo(user.getCash());

    }

    @Test
    public void userRepository_FindUserId_ReturnLong() {

        //Arrange
        Users userTest = Users.builder()
                .username("userTest")
                .password("userTest")
                .build();

        usersRepo.save(userTest);

        //Act
        Long userTestId = usersRepo.findUserId(
                userTest.getUsername());

        //Assert
        Assertions.assertThat(userTestId).isNotNull();
        Assertions.assertThat(userTestId)
                .isEqualTo(userTest.getId());
    }

    @Test
    public void userRepository_findUserPassword_ReturnString() {

        //Arrange
        Users userTest = Users.builder()
                .username("userTest")
                .password("userTest")
                .build();

        usersRepo.save(userTest);

        //Act
        String userPasswordTest = usersRepo.findUserPassword(
                userTest.getUsername());

        //Assert
        Assertions.assertThat(userPasswordTest).isNotNull();
        Assertions.assertThat(userPasswordTest)
                .isEqualTo(userTest.getPassword());
    }

    @Test
    public void userRepository_findUserUsername_ReturnString() {

        //Arrange
        Users userTest = Users.builder()
                .username("userTest")
                .password("userTest")
                .build();

        usersRepo.save(userTest);

        //Act
        String usernameTest = usersRepo.findUserUsername(
                userTest.getId());

        //Assert
        Assertions.assertThat(usernameTest).isNotNull();
        Assertions.assertThat(usernameTest)
                .isEqualTo(userTest.getUsername());
    }

    @Test
    public void userRepository_changeCash_ReturnNofChangedRows() {

        //Arrange
        BigDecimal userCash = BigDecimal.valueOf(100);
        BigDecimal valueToChange = BigDecimal.valueOf(200);
        Users userTest = Users.builder()
                .username("userTest")
                .password("userTest")
                .cash(userCash)
                .build();

        usersRepo.save(userTest);

        //Act
        int changeCashResult = usersRepo.changeCash(
                valueToChange, userTest.getUsername());

        BigDecimal userCashChanged = usersRepo.findUserCash(
                userTest.getUsername());

        //Assert
        Assertions.assertThat(changeCashResult).isEqualTo(1);
        Assertions.assertThat(userCashChanged)
                .isEqualByComparingTo(valueToChange);


    }

    @Test
    public void UserRepository_ChangeUsername_ReturnNofChangedRows() {
        //Arrange
        String usernameBeforeChange = "userTest";
        String usernameAfterChange = "userTested";
        Users userTest = new Users(usernameBeforeChange,
                "UserTest@1234");

        userTest = usersRepo.save(userTest);

        //Act
        int changeUsernameResult = usersRepo.changeUsername(
                usernameAfterChange, userTest.getId());
        String updatedUser = usersRepo
                .findUserUsername(userTest.getId());

        //Assert
        Assertions.assertThat(changeUsernameResult)
                .isEqualTo(1);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser)
                .isEqualTo(usernameAfterChange);

    }

    @Test
    public void UserRepository_ChangePassword_ReturnNofChangedRows() {
        //Arrange
        String passwordBeforeChange = "UserTest@1234";
        String passwordAfterChange = "UserTested@1234";
        Users userTest = new Users("userTest",
                passwordBeforeChange);

        userTest = usersRepo.save(userTest);

        //Act
        int changePasswordResult = usersRepo.changePassword(
                passwordAfterChange, userTest.getId());
        String updatedUser = usersRepo
                .findUserPassword(userTest.getUsername());

        //Assert
        Assertions.assertThat(changePasswordResult)
                .isEqualTo(1);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser)
                .isEqualTo(passwordAfterChange);

    }
}
