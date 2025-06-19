package com.b2b.ordermanagement.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private boolean connectionFailure = false;

    public boolean simulateMessageSend(String topic, String message) {
        try {
            if (connectionFailure) {
                logger.error("RabbitMQ connection failed for topic '{}'", topic);
                return false;
            }

            Thread.sleep(10); // Simulate network latency
            logger.debug("Message sent to topic '{}': {}", topic, message);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Error sending notification", e);
            return false;
        }
    }
}
