package org.ciyex.sdk.notifications;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Generic notification provider interface (SMS, Email, Push, Voice).
 * Vendor adapters (Twilio, Mailgun, SendGrid, etc.) implement this.
 */
public interface NotificationProvider {

    /** Send a single notification. */
    SendResult send(NotificationRequest request);

    /** Send batch notifications (campaigns, reminders). */
    BatchResult sendBatch(List<NotificationRequest> requests);

    /** Get delivery status of a notification. */
    DeliveryStatus getDeliveryStatus(String notificationId);

    /** List incoming messages (for two-way SMS). */
    List<InboundMessage> listInbound(String orgAlias, String channel, int limit, int offset);

    /** Register a phone number or sender identity. */
    SenderIdentity registerSender(String orgAlias, String channel, String identifier);

    /** Verify connectivity. */
    ConnectionStatus testConnection(Map<String, String> config);

    String vendorId();

    // --- DTOs ---

    record NotificationRequest(
            String orgAlias,
            String channel, // SMS, EMAIL, VOICE, PUSH
            String to,
            String from,
            String subject,
            String body,
            String templateId,
            Map<String, String> templateVariables,
            Instant scheduledAt,
            Map<String, String> metadata
    ) {}

    record SendResult(
            String notificationId,
            String status, // QUEUED, SENT, DELIVERED, FAILED
            String channel,
            Instant sentAt
    ) {}

    record BatchResult(
            int totalRequested,
            int queued,
            int failed,
            List<String> failedRecipients
    ) {}

    record DeliveryStatus(
            String notificationId,
            String status, // QUEUED, SENT, DELIVERED, FAILED, BOUNCED, OPENED, CLICKED
            Instant updatedAt,
            String errorMessage
    ) {}

    record InboundMessage(
            String messageId,
            String from,
            String to,
            String body,
            String channel,
            Instant receivedAt,
            boolean read
    ) {}

    record SenderIdentity(
            String senderId,
            String channel,
            String identifier,
            String status, // PENDING, VERIFIED, REJECTED
            String orgAlias
    ) {}

    record ConnectionStatus(boolean connected, String message) {}
}
