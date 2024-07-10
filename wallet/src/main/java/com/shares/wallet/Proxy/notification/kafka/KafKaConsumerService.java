package com.shares.wallet.Proxy.notification.kafka;

import com.shares.wallet.dto.NotificationRequest;
import com.shares.wallet.exceptions.ConsumerException;
import com.shares.wallet.model.History;
import com.shares.wallet.model.StockQuote;
import com.shares.wallet.services.NotificationService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;

@Service
public class KafKaConsumerService {

    private static final Logger consumerLogger = LoggerFactory.getLogger(KafKaConsumerService.class);

    @Getter
    private History payload;
    @Getter
    private final String topic = "transaction-notification";


    private final NotificationService notificationService;
    public KafKaConsumerService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = {topic}, groupId = "transaction-email-notification")
    public void consumeTransaction(History history) {

        consumerLogger.info("message consumed inside topic: {} with payload: {}", topic, history);

        payload = history;

        notificationService.sendEmailViaAPi(history);

    }

}

