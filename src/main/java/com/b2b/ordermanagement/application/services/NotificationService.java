package com.b2b.ordermanagement.application.services;

import com.b2b.ordermanagement.domain.entities.Order;
import com.b2b.ordermanagement.domain.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    protected void simulateMessageSend(String topic, String message) {
        // Simulate async message publishing
        try {
            Thread.sleep(10); // Simulate network latency
            logger.debug("Message sent to topic '{}': {}", topic, message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Error sending notification", e);
        }
    }
}