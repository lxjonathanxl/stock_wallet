package com.shares.wallet.services;

import com.shares.wallet.exceptions.UsernameTakenException;
import com.shares.wallet.model.MessageController;
import com.shares.wallet.model.Users;
import com.shares.wallet.repo.UsersRepo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsersServiceTest {

    @Mock
    private UsersRepo usersRepo;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UsersService usersService;

    private Users userTest;

    @BeforeEach
    public void init() {
        userTest = Users.builder()
                .username("usernameTest")
                .password("Test@1234")
                .build();
    }

    @Test
    public void usersService_signUpUser_ReturnVoid_Success() {
        //Arrange
        String rawPassword = userTest.getPassword();
        String encryptedPassword = "encryptedTest@1234";

        when(usersRepo.findByUsername(userTest.getUsername()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(userTest.getPassword()))
                .thenReturn(encryptedPassword);

        //Act
        boolean result = usersService.signUpUser(userTest);

        //Assert
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void usersService_signUpUser_ReturnVoid_Fail_UsernameTaken() {

        //Arrange
        when(usersRepo.findByUsername(userTest.getUsername()))
                .thenReturn(Optional.of(userTest));

        //Act and assert
        assertThatThrownBy(() -> usersService.signUpUser(userTest))
                .isInstanceOf(UsernameTakenException.class)
                .hasMessageContaining("username already taken");
    }

    @Test
    public void usersService_changeUsername_returnMessageController_Success() {

        //Arrange
        String username = userTest.getUsername();
        String password = userTest.getPassword();
        Long userId = userTest.getId();
        String newUsername = "usernameChanged";

        when(usersRepo.findByUsername(username))
                .thenReturn(Optional.of(userTest));
        when(usersRepo.findUserPassword(username))
                .thenReturn(password);
        when(passwordEncoder.matches(password, password))
                .thenReturn(true);
        when(usersRepo.findByUsername(newUsername))
                .thenReturn(Optional.empty());
        when(usersRepo.findUserId(username))
                .thenReturn(userId);
        when(usersRepo.changeUsername(newUsername, userId))
                .thenReturn(1);

        //Act
        MessageController result = usersService
                .changeUsername(username, password, newUsername);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isTrue();
        Assertions.assertThat(result.getMessage())
                .isEqualTo("Username changed");
    }

    @Test
    public void usersService_changeUsername_returnMessageController_Fail_UserNotFound() {

        //Arrange
        String username = userTest.getUsername();
        String password = userTest.getPassword();
        String newUsername = "usernameChanged";

        when(usersRepo.findByUsername(username))
                .thenReturn(Optional.empty());

        //Act
        MessageController result = usersService
                .changeUsername(username, password, newUsername);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isFalse();
        Assertions.assertThat(result.getMessage())
                .isEqualTo("Server error: unable to find user");
    }

    @Test
    public void usersService_changeUsername_returnMessageController_Fail_WrongPassword() {

        //Arrange
        String username = userTest.getUsername();
        String password = userTest.getPassword();
        String newUsername = "usernameChanged";


        when(usersRepo.findByUsername(username))
                .thenReturn(Optional.of(userTest));
        when(usersRepo.findUserPassword(username))
                .thenReturn(password);
        when(passwordEncoder.matches(password, password))
                .thenReturn(false);

        //Act
        MessageController result = usersService
                .changeUsername(username, password, newUsername);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isFalse();
        Assertions.assertThat(result.getMessage())
                .isEqualTo("wrong password!!");
    }

    @Test
    public void usersService_changeUsername_returnMessageController_Fail_UsernameTaken() {

        //Arrange
        String username = userTest.getUsername();
        String password = userTest.getPassword();
        String newUsername = "usernameChanged";

        when(usersRepo.findByUsername(username))
                .thenReturn(Optional.of(userTest));
        when(usersRepo.findUserPassword(username))
                .thenReturn(password);
        when(passwordEncoder.matches(password, password))
                .thenReturn(true);
        when(usersRepo.findByUsername(newUsername))
                .thenReturn(Optional.of(userTest));

        //Act
        MessageController result = usersService
                .changeUsername(username, password, newUsername);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isFalse();
        Assertions.assertThat(result.getMessage())
                .isEqualTo("username unavailable");
    }

    @Test
    public void usersService_changePassword_ReturnMessageController_success() {

        //Arrange
        String username = userTest.getUsername();
        String oldPassword = userTest.getPassword();
        String newPassword = "Test@1234";
        String encodedNewPassword = "Test@1234Encoded";
        Long userId = userTest.getId();

        when(usersRepo.findByUsername(username))
                .thenReturn(Optional.of(userTest));
        when(usersRepo.findUserPassword(username))
                .thenReturn(oldPassword);
        when(passwordEncoder.matches(oldPassword, oldPassword))
                .thenReturn(true);
        when(usersRepo.findUserId(username))
                .thenReturn(userId);
        when(passwordEncoder.encode(newPassword))
                .thenReturn(encodedNewPassword);
        when(usersRepo.changePassword(encodedNewPassword, userId))
                .thenReturn(1);

        //Act
        MessageController result = usersService
                .changePassword(username, oldPassword, newPassword);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isTrue();
        Assertions.assertThat(result.getMessage())
                .isEqualTo("Password changed");

    }

    @Test
    public void usersService_changePassword_ReturnMessageController_Fail_UserNotFound() {

        //Arrange
        String username = userTest.getUsername();
        String password = userTest.getPassword();
        String newPassword = "Test@1234";

        when(usersRepo.findByUsername(username))
                .thenReturn(Optional.empty());

        //Act
        MessageController result = usersService
                .changePassword(username, password, newPassword);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isFalse();
        Assertions.assertThat(result.getMessage())
                .isEqualTo("Server error: unable to find user");

    }

    @Test
    public void usersService_changePassword_ReturnMessageController_Fail_WrongPassword() {

        //Arrange
        String username = userTest.getUsername();
        String wrongPassword = "wrongTest";
        String userPassword = userTest.getPassword();
        String newPassword = "Test@1234";


        when(usersRepo.findByUsername(username))
                .thenReturn(Optional.of(userTest));
        when(usersRepo.findUserPassword(username))
                .thenReturn(userPassword);
        when(passwordEncoder.matches(wrongPassword, userPassword))
                .thenReturn(false);

        //Act
        MessageController result = usersService
                .changePassword(username, wrongPassword, newPassword);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isFalse();
        Assertions.assertThat(result.getMessage())
                .isEqualTo("wrong password!!");

    }

    @Test
    public void usersService_changePassword_ReturnMessageController_Fail_dontMatchesRegex() {

        //Arrange
        String username = userTest.getUsername();
        String oldPassword = userTest.getPassword();
        String newPassword = "testDontMatchesRegex";


        when(usersRepo.findByUsername(username))
                .thenReturn(Optional.of(userTest));
        when(usersRepo.findUserPassword(username))
                .thenReturn(oldPassword);
        when(passwordEncoder.matches(oldPassword, oldPassword))
                .thenReturn(true);

        //Act
        MessageController result = usersService
                .changePassword(username, oldPassword, newPassword);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isFalse();
        Assertions.assertThat(result.getMessage())
                .isEqualTo("new password is weak");

    }

}
