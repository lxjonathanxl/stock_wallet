package com.shares.wallet.services;

import com.shares.wallet.Proxy.notification.kafka.KafkaProducerService;
import com.shares.wallet.model.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger notificationLogger = LoggerFactory.getLogger(NotificationService.class);
    private final KafkaProducerService producerService;
    public NotificationService(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    public void notify(History history) {
        notificationLogger.info("Calling producer to send message to topic");
        producerService.send("transaction-notification", history);
    }
}
