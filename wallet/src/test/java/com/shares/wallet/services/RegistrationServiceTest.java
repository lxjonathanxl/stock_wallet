package com.shares.wallet.services;

import com.shares.wallet.dto.RegistrationRequest;
import com.shares.wallet.exceptions.DatabaseException;
import com.shares.wallet.exceptions.UsernameTakenException;
import com.shares.wallet.model.MessageController;
import com.shares.wallet.model.Users;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private UsersService usersService;

    @InjectMocks
    private RegistrationService registrationService;

    private RegistrationRequest request;

    @BeforeEach
    public void init() {
        request = RegistrationRequest.builder()
                .username("userTest")
                .password("Test@1234")
                .email("emailTest@gmail.com")
                .confirmation("Test@1234")
                .build();
    }

    @Test
    public void registrationService_register_ReturnMessageController_success() {

        //Arrange
        when(usersService.signUpUser(Mockito.any(Users.class)))
                .thenReturn(true);

        //Act
        MessageController result = registrationService.register(request);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isTrue();
        Assertions.assertThat(result.getMessage())
                .isEqualTo("Registration complete");
    }

    @Test
    public void registrationService_register_ReturnMessageController_fail_UsernameTaken() {
        //Arrange
        when(usersService.signUpUser(Mockito.any(Users.class)))
                .thenThrow(UsernameTakenException.class);

        //Act
        MessageController result = registrationService.register(request);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isFalse();
        Assertions.assertThat(result.getMessage())
                .isEqualTo("Username taken");
    }

    @Test
    public void registrationService_register_ReturnMessageController_fail_ServerError() {
        //Arrange
        when(usersService.signUpUser(Mockito.any(Users.class)))
                .thenThrow(DatabaseException.class);

        //Act
        MessageController result = registrationService.register(request);

        //Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getSucceeded()).isFalse();
        Assertions.assertThat(result.getMessage()).isEqualTo("Error registering User, please try again");
    }
}
