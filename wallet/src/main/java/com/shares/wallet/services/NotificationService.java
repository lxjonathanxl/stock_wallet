package com.shares.wallet.services;

import com.shares.wallet.Proxy.notification.kafka.KafkaProducerService;
import com.shares.wallet.dto.NotificationRequest;
import com.shares.wallet.exceptions.ConsumerException;
import com.shares.wallet.model.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationService {

    private static final Logger notificationLogger = LoggerFactory.getLogger(NotificationService.class);
    private final KafkaProducerService producerService;
    private final RestTemplate restTemplate;

    private String apiUrl;

    public NotificationService(KafkaProducerService producerService, RestTemplate restTemplate,
                               @Value("${wallet.notificationApiProxy.url}") String apiUrl) {
        this.producerService = producerService;
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    public void notify(History history) {
        notificationLogger.info("Calling producer to send history message to topic");
        producerService.send("transaction-notification", history);
    }

    public Boolean sendEmailViaAPi(History history) {

        try {

            NotificationRequest notification = new NotificationRequest(
                    history.getUser().getEmail(),
                    history.getUser().getUsername(),
                    history.getAction(),
                    history.getQuant(),
                    history.getName(),
                    history.getDate().toString(),
                    history.getPrice());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<NotificationRequest> requestEntity = new HttpEntity<>(notification, headers);

            ResponseEntity<Boolean> responseEntity = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, requestEntity, Boolean.class
            );

            Boolean result = responseEntity.getBody();

            if (result == null) {
                notificationLogger.error("Error getting response from notification api");
                throw new ConsumerException("bad response");
            } else if (!result) {
                notificationLogger.error("api returned false as result");
                throw new ConsumerException("operation failed");

            }

            notificationLogger.info("api returned true");
            return true;

        } catch (HttpClientErrorException.BadRequest ex) {
            notificationLogger.error("history generated a bad notification request" +
                    "History: {}", history.toString());
            throw new ConsumerException("bad request");
        }
    }
}
