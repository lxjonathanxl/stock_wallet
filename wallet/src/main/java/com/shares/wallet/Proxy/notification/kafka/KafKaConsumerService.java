package com.shares.wallet.Proxy.notification.kafka;

import com.shares.wallet.model.History;
import com.shares.wallet.model.StockQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class KafKaConsumerService {

    private static final Logger consumerLogger = LoggerFactory.getLogger(KafKaConsumerService.class);
    private final RestTemplate restTemplate;
    public KafKaConsumerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = {"transaction-notification"}, groupId = "transaction-email-notification")
    public void consumeTransaction(History history) {

        try {

            String apiUrl = "http://email-notification-api:8093/get/result";

            Boolean result = restTemplate.getForObject(apiUrl, Boolean.class);

            if (result == null) {
                consumerLogger.error("Error getting response from notification api");
                throw new RuntimeException("bad response");
            } else if (!result) {
                consumerLogger.error("api returned false as result");
                throw new RuntimeException("operation failed");

            }

            consumerLogger.info("api returned true");

        } catch (HttpClientErrorException.BadRequest ex) {
            consumerLogger.error("bad request");
            throw new RuntimeException("bad request");
        }

    }
}

