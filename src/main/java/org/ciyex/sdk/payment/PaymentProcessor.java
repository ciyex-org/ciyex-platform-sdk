package org.ciyex.sdk.payment;

import org.ciyex.sdk.common.Money;

import java.util.Map;

/**
 * Generic payment processor interface.
 * Vendor adapters (Stripe, Global Payments, Square, etc.) implement this.
 * The SDK discovers the active vendor from Hub config at runtime.
 *
 * <pre>
 * // Vendor adapter example:
 * &#64;Component
 * public class StripePaymentProcessor implements PaymentProcessor { ... }
 * </pre>
 */
public interface PaymentProcessor {

    /**
     * Create a payment intent/session for collecting payment.
     *
     * @param request payment details
     * @return result with client-side token for UI completion
     */
    PaymentResult createPayment(PaymentRequest request);

    /**
     * Capture a previously authorized payment.
     */
    PaymentResult capturePayment(String paymentId, Money amount);

    /**
     * Refund a completed payment (full or partial).
     */
    RefundResult refund(String paymentId, Money amount, String reason);

    /**
     * Void/cancel an uncaptured authorization.
     */
    PaymentResult voidPayment(String paymentId);

    /**
     * Tokenize a payment method for future use (PCI-compliant).
     *
     * @return token that can be stored safely
     */
    TokenResult tokenize(TokenizeRequest request);

    /**
     * Delete a stored payment method token.
     */
    void deleteToken(String tokenId);

    /**
     * Verify processor connectivity and credentials.
     */
    ConnectionStatus testConnection(Map<String, String> config);

    /**
     * Process a webhook event from the payment processor.
     *
     * @param payload raw webhook body
     * @param signature webhook signature header
     * @return parsed event
     */
    WebhookEvent parseWebhook(String payload, String signature);

    /** Vendor identifier (e.g., "stripe", "gps", "square"). */
    String vendorId();

    // --- DTOs ---

    record PaymentRequest(
            String orgAlias,
            String patientId,
            Money amount,
            String currency,
            String description,
            String paymentMethodToken,
            boolean captureImmediately,
            Map<String, String> metadata
    ) {}

    record PaymentResult(
            String paymentId,
            String status,
            Money amount,
            String clientSecret,
            String receiptUrl,
            Map<String, Object> vendorData
    ) {}

    record RefundResult(
            String refundId,
            String paymentId,
            Money amount,
            String status
    ) {}

    record TokenizeRequest(
            String orgAlias,
            String patientId,
            String paymentMethodType,
            Map<String, String> cardDetails
    ) {}

    record TokenResult(
            String tokenId,
            String last4,
            String brand,
            String expiryMonth,
            String expiryYear,
            String paymentMethodType
    ) {}

    record ConnectionStatus(
            boolean connected,
            String message,
            String environment
    ) {}

    record WebhookEvent(
            String eventType,
            String paymentId,
            Map<String, Object> data
    ) {}
}
