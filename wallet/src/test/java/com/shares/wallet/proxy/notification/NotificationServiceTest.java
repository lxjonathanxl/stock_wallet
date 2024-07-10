package com.shares.wallet.proxy.notification;

import com.shares.wallet.Proxy.notification.kafka.KafkaProducerService;
import com.shares.wallet.exceptions.ConsumerException;
import com.shares.wallet.model.History;
import com.shares.wallet.model.Users;
import com.shares.wallet.services.NotificationService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class NotificationServiceTest {

    private NotificationService notificationService;
    private RestTemplate restTemplate;
    private String apiUrl = "http://localhost:8093/sendEmail";
    @MockBean
    private KafkaProducerService kafkaProducerService;
    private History historyTest;
    @BeforeEach
    public void init() {
        restTemplate = new RestTemplate();
        notificationService =
                new NotificationService(kafkaProducerService, restTemplate, apiUrl);

        Users userTest = Users
                .builder()
                .username("userTest")
                .email("stockwalletfinance@gmail.com")
                .build();

        historyTest = History
                .builder()
                .user(userTest)
                .action("buy")
                .quant(new BigDecimal("100.50"))
                .name("AAPL")
                .date(new Date())
                .price(new BigDecimal("150.75"))
                .build();
    }

    @Test
    public void notificationService_sendEmailViaApi_returnBoolean_success() {

        //act
        Boolean result = notificationService.sendEmailViaAPi(historyTest);

        //assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void notificationService_sendEmailViaApi_returnBoolean_failed_invalidEmail() {

        //Assert
        Users userTestInvalid = Users
                .builder()
                .username("userTest")
                .email("invalidEmail")
                .build();

        historyTest.setUser(userTestInvalid);

        //act and Assert
        assertThatThrownBy(() -> notificationService.sendEmailViaAPi(historyTest))
                .isInstanceOf(ConsumerException.class);
    }
}
