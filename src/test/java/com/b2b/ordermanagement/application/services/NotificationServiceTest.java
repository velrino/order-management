package com.b2b.ordermanagement.application.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    void shouldReturnTrueWhenMessageIsSentSuccessfully() {
        boolean result = notificationService.simulateMessageSend("orders", "Order #123 created");
        assertTrue(result, "Expected message to be sent successfully");
    }

    @Test
    void shouldReturnFalseWhenConnectionFailureOccurs() throws Exception {
        var field = NotificationService.class.getDeclaredField("connectionFailure");
        field.setAccessible(true);
        field.set(notificationService, true);

        boolean result = notificationService.simulateMessageSend("orders", "Order #123 failed");
        assertFalse(result, "Expected message sending to fail due to connection failure");
    }

    @Test
    void shouldInterruptThreadGracefully() throws Exception {
        Thread.currentThread().interrupt();

        boolean result = notificationService.simulateMessageSend("orders", "Check interrupt");

        assertFalse(result, "Expected message sending to fail due to interruption");
        assertTrue(Thread.interrupted(), "Thread should remain interrupted after handling");
    }
}
