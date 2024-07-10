package controllersTest;


import com.api.notificationApi.NotificationApiApplication;
import com.api.notificationApi.controllers.NotificationController;
import com.api.notificationApi.dto.NotificationRequest;
import com.api.notificationApi.service.EmailSenderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ExtendWith(MockitoExtension.class)
@WebMvcTest(NotificationController.class)
@ContextConfiguration(classes = NotificationApiApplication.class)
public class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    EmailSenderService emailSenderService;

    @Test
    public void sendEmail_succeed() throws Exception {
        //Arrange
        when(emailSenderService.sendEmail(any(NotificationRequest.class)))
                .thenReturn(true);

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .email("example@gmail.com")
                .username("exampleUser")
                .action("exampleAction")
                .quant(new BigDecimal("100.00"))
                .name("exampleName")
                .date("2024-07-09")
                .price(new BigDecimal("1000.00"))
                .build();

        String notificationRequestJson = objectMapper.writeValueAsString(notificationRequest);


        mockMvc.perform(post("http://localhost:8093/sendEmail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationRequestJson))
                .andExpect(status().isOk());
    }

    @Test
    public void sendEmail_fail_invalidEmail() throws Exception {
        //Arrange
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .email("invalidEmail")
                .username("exampleUser")
                .action("exampleAction")
                .quant(new BigDecimal("100.00"))
                .name("exampleName")
                .date("2024-07-09")
                .price(new BigDecimal("1000.00"))
                .build();

        String notificationRequestJson = objectMapper.writeValueAsString(notificationRequest);


        mockMvc.perform(post("http://localhost:8093/sendEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notificationRequestJson))
                .andExpect(status().isBadRequest());
    }
}
