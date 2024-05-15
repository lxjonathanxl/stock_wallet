package com.shares.wallet.controllers;

import com.shares.wallet.dto.RegistrationRequest;
import com.shares.wallet.model.MessageController;
import com.shares.wallet.security.config.WebSecurityConfig;
import com.shares.wallet.services.RegistrationService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(RegistrationController.class)
@Import(WebSecurityConfig.class)
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @Test
    void registerPost_successful() throws Exception {
        //Arrange
        MessageController resultTest = MessageController
                .builder()
                .message("Registration successful")
                .succeeded(true)
                .build();

        when(registrationService.register(any(RegistrationRequest.class)))
                .thenReturn(resultTest);

        //Act and Assert
        mockMvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(EntityUtils.toString(
                        new UrlEncodedFormEntity(Arrays.asList(
                                new BasicNameValuePair(
                                        "username", "usertest"
                                ),
                                new BasicNameValuePair(
                                        "password", "Test@1234"
                                ),
                                new BasicNameValuePair(
                                        "confirmation", "Test@1234"
                                )
                        )

                        )
                )))
                .andExpect(view().name("redirect:/login"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("message", resultTest.getMessage()));
    }

    @Test
    void registerPost_fail_emptyUsername() throws Exception {
        //Arrange
        String message = "username cant be empty";

        //Act and Assert
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "username", ""
                                        ),
                                        new BasicNameValuePair(
                                                "password", "Test@1234"
                                        ),
                                        new BasicNameValuePair(
                                                "confirmation", "Test@1234"
                                        )
                                )

                                )
                        )))
                .andExpect(view().name("redirect:/register"))
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void registerPost_fail_weakPassword() throws Exception {
        //Arrange
        String message = "Password doesn't meet the criteria";

        //Act and Assert
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "username", "userTest"
                                        ),
                                        new BasicNameValuePair(
                                                "password", "test1234"
                                        ),
                                        new BasicNameValuePair(
                                                "confirmation", "test1234"
                                        )
                                )

                                )
                        )))
                .andExpect(view().name("redirect:/register"))
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void registerPost_fail_passwordMismatch() throws Exception {
        //Arrange
        String message = "Password and confirmation do not match";

        //Act and Assert
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "username", "userTest"
                                        ),
                                        new BasicNameValuePair(
                                                "password", "Test@1234"
                                        ),
                                        new BasicNameValuePair(
                                                "confirmation", "wrongPassword"
                                        )
                                )

                                )
                        )))
                .andExpect(view().name("redirect:/register"))
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void registerPost_fail_registrationService() throws Exception {
        //Arrange
        MessageController resultTest = MessageController
                .builder()
                .message("Registration failed")
                .succeeded(false)
                .build();

        when(registrationService.register(any(RegistrationRequest.class)))
                .thenReturn(resultTest);

        //Act and Assert
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(EntityUtils.toString(
                                new UrlEncodedFormEntity(Arrays.asList(
                                        new BasicNameValuePair(
                                                "username", "usertest"
                                        ),
                                        new BasicNameValuePair(
                                                "password", "Test@1234"
                                        ),
                                        new BasicNameValuePair(
                                                "confirmation", "Test@1234"
                                        )
                                )

                                )
                        )))
                .andExpect(view().name("redirect:/register"))
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("message", resultTest.getMessage()));
    }
}
