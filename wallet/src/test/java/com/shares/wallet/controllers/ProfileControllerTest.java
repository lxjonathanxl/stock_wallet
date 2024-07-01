package com.shares.wallet.controllers;

import com.shares.wallet.model.MessageController;
import com.shares.wallet.security.config.WebSecurityConfig;
import com.shares.wallet.services.UsersService;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@Import(WebSecurityConfig.class)
@WebMvcTest(ProfileController.class)
@WithMockUser
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;

    @Test
    void getProfile_succeed() throws Exception {
        //Arrange
        String username = "user";
        //Act and Assert
        mockMvc.perform(get("/profile"))
                .andExpect(view().name("profile.html"))
                .andExpect(model().attribute("username", username));

    }

    @Test
    void postChangeUsername_succeed() throws Exception {
        //Arrange
        String username = "user";
        String password = "Test@1234";
        String newUsername = "userTest";

        MessageController resultTest = MessageController
                .builder()
                .message("Username changed")
                .succeeded(true)
                .build();

        when(usersService.changeUsername(username, password, newUsername))
                .thenReturn(resultTest);

        //Act and Assert
        mockMvc.perform(post("/profileUsername")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(
                        new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair(
                                        "username", newUsername
                                ),
                                new BasicNameValuePair(
                                        "password", password
                                )
                        )
                        )
                )))
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", resultTest.getMessage()));
    }

    @Test
    void postChangeUsername_fail_usersService() throws Exception {
        //Arrange
        String username = "user";
        String password = "Test@1234";
        String newUsername = "userTest";

        MessageController resultTest = MessageController
                .builder()
                .message("Change username failed")
                .succeeded(false)
                .build();

        when(usersService.changeUsername(username, password, newUsername))
                .thenReturn(resultTest);

        //Act and Assert
        mockMvc.perform(post("/profileUsername")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(
                        new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair(
                                        "username", newUsername
                                ),
                                new BasicNameValuePair(
                                        "password", password
                                )
                        ))
                )))
                .andExpect(view().name("redirect:/profile"))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("message", resultTest.getMessage()));
    }

    @Test
    void postChangePassword_succeed() throws Exception {
        String username = "user";
        String oldPassword = "Test@1234";
        String newPassword = "newTest@1234";
        String confirmNewPassword = "newTest@1234";

        MessageController resultTest = MessageController
                .builder()
                .message("Password changed")
                .succeeded(true)
                .build();

        when(usersService.changePassword(username, oldPassword, newPassword))
                .thenReturn(resultTest);

        //Act and Assert
        mockMvc.perform(post("/profilePassword")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(
                        new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair("password", oldPassword),
                                new BasicNameValuePair("new_password", newPassword),
                                new BasicNameValuePair("confirm_new_password", confirmNewPassword)
                        ))
                )))
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", resultTest.getMessage()));
    }

    @Test
    void postChangePassword_fail_passwordMismatch() throws Exception {
        //Arrange
        String oldPassword = "Test@1234";
        String newPassword = "newTest@1234";
        String confirmNewPassword = "wrongPassword";

        String message = "new password and confirmation mismatch";

        //Act and Assert
        mockMvc.perform(post("/profilePassword")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(
                        new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair("password", oldPassword),
                                new BasicNameValuePair("new_password", newPassword),
                                new BasicNameValuePair("confirm_new_password", confirmNewPassword)
                        ))
                )))
                .andExpect(view().name("redirect:/profile"))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("message", message));

    }

    @Test
    void postChangePassword_fail_usersService() throws Exception {
        //Arrange
        String username = "user";
        String oldPassword = "Test@1234";
        String newPassword = "newTest@1234";
        String confirmNewPassword = "newTest@1234";

        MessageController resultTest = MessageController
                .builder()
                .message("Change password failed")
                .succeeded(false)
                .build();

        when(usersService.changePassword(username, oldPassword, newPassword))
                .thenReturn(resultTest);

        //Act and Assert
        mockMvc.perform(post("/profilePassword")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(
                        new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair("password", oldPassword),
                                new BasicNameValuePair("new_password", newPassword),
                                new BasicNameValuePair("confirm_new_password", confirmNewPassword)
                        ))
                )))
                .andExpect(view().name("redirect:/profile"))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("message", resultTest.getMessage()));
    }

    @Test
    void postChangeEmail_succeed() throws Exception {
        String username = "user";
        String password = "Test@1234";
        String newEmail = "testEmail@gmail.com";

        MessageController resultTest = MessageController
                .builder()
                .message("email changed")
                .succeeded(true)
                .build();

        when(usersService.changeEmail(username, password, newEmail))
                .thenReturn(resultTest);

        //Act and Assert
        mockMvc.perform(post("/profileEmail")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("email", newEmail),
                                        new BasicNameValuePair("password", password)
                                ))
                        )))
                .andExpect(view().name("redirect:/profile"))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("message", resultTest.getMessage()));
    }

    @Test
    void postChangeEmail_fail_invalidEmail() throws Exception {
        //Arrange
        String message = "Invalid email";

        //Act and Assert
        mockMvc.perform(post("/profileEmail")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "email", "invalidEmail"
                                        ),
                                        new BasicNameValuePair(
                                                "password", "Test@1234"
                                        )
                                )

                                )
                        )))
                .andExpect(view().name("redirect:/profile"))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void postChangeEmail_fail_usersService() throws Exception {
        //Arrange
        String username = "user";
        String password = "Test@1234";
        String newEmail = "testEmail@gmail.com";

        MessageController resultTest = MessageController
                .builder()
                .message("Change email failed")
                .succeeded(false)
                .build();

        when(usersService.changeEmail(username, password, newEmail))
                .thenReturn(resultTest);

        //Act and Assert
        mockMvc.perform(post("/profileEmail")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("email", newEmail),
                                        new BasicNameValuePair("password", password)
                                ))
                        )))
                .andExpect(view().name("redirect:/profile"))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("message", resultTest.getMessage()));
    }

    @Test
    void postAddCashController_succeed() throws Exception {
        //Arrange
        String username = "user";
        BigDecimal cashToAdd = BigDecimal.valueOf(20);
        String password = "Test@1234";

        String message = "cash added to wallet";

        when(usersService.changeCashUserProfile(username, password, cashToAdd))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/profileCash")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(
                        new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair("cash", "20"),
                                new BasicNameValuePair("password", password)
                        ))
                )))
                .andExpect(view().name("redirect:/profile"))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void postAddCashController_fail_wrongPassword() throws Exception {
        //Arrange
        String username = "user";
        String password = "TestWrong@1234";
        BigDecimal cashToAdd = BigDecimal.valueOf(20);

        String message = "wrong password";

        when(usersService.changeCashUserProfile(username, password, cashToAdd))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/profileCash")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("cash", "20"),
                                        new BasicNameValuePair("password", password)
                                ))
                        )))
                .andExpect(view().name("redirect:/profile"))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void postAddCashController_fail_dataError() throws Exception {
        //Arrange
        String username = "user";
        String password = "Test@1234";
        BigDecimal cashToAdd = BigDecimal.valueOf(20);

        String message = "server error: " +
                "handling users wallet";

        when(usersService.changeCashUserProfile(username, password, cashToAdd))
                .thenReturn(message);

        //Act and Assert
        mockMvc.perform(post("/profileCash")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair("cash", "20"),
                                        new BasicNameValuePair("password", password)
                                ))
                        )))
                .andExpect(view().name("redirect:/profile"))
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("message", message));
    }
}
