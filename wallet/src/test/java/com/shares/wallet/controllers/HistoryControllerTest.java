package com.shares.wallet.controllers;

import com.shares.wallet.exceptions.HistoryNotFoundException;
import com.shares.wallet.model.History;
import com.shares.wallet.security.config.WebSecurityConfig;
import com.shares.wallet.services.TransactionService;
import com.shares.wallet.services.UsersService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@Import(WebSecurityConfig.class)
@WebMvcTest(HistoryController.class)
@WithMockUser
public class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void getHistory_succeed() throws Exception {
        //Arrange
        String username = "user";

        List<History> historyTest = new ArrayList<>();
        historyTest.add(History.builder().action("buy").build());
        historyTest.add(History.builder().action("sell").build());

        when(transactionService.displayUserHistory(username))
                .thenReturn(historyTest);

        //Act and Assert
        mockMvc.perform(get("/history"))
                .andExpect(view().name("history.html"))
                .andExpect(model().attribute("history", historyTest));
    }

    @Test
    void getHistory_fail_userError() throws Exception {
        //Arrange
        String username = "user";
        String message = "Error looking for user history, username not found in database";

        when(transactionService.displayUserHistory(username))
                .thenThrow(UsernameNotFoundException.class);

        //Act and Assert
        mockMvc.perform(get("/history"))
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attribute("message", message));
    }

    @Test
    void getHistory_fail_historyError() throws Exception {
        //Arrange
        String username = "user";
        String message = "Error looking for user history, history not found in database";

        when(transactionService.displayUserHistory(username))
                .thenThrow(HistoryNotFoundException.class);

        //Act and Assert
        mockMvc.perform(get("/history"))
                .andExpect(view().name("redirect:/"))
                .andExpect(flash().attribute("message", message));
    }
}
