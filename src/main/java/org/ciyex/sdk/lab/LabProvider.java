package org.ciyex.sdk.lab;

import org.ciyex.sdk.common.PatientRef;
import org.ciyex.sdk.common.ProviderRef;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Generic lab integration provider interface.
 * Vendor adapters (Health Gorilla, Quest, Labcorp, etc.) implement this.
 */
public interface LabProvider {

    /** Submit a lab order electronically. */
    OrderResult submitOrder(LabOrder order);

    /** Cancel a pending lab order. */
    OrderResult cancelOrder(String orderId, String reason);

    /** Get order status and tracking. */
    OrderStatus getOrderStatus(String orderId);

    /** Retrieve lab results for an order. */
    LabResults getResults(String orderId);

    /** Search test compendium. */
    List<LabTest> searchTests(String query, String labId, int limit);

    /** List available labs by location. */
    List<LabFacility> searchLabs(String zipCode, int radiusMiles);

    /** Get AOE (Ask at Order Entry) questions for a test. */
    List<AoeQuestion> getAoeQuestions(String testCode, String labId);

    /** Verify connectivity with lab network. */
    ConnectionStatus testConnection(Map<String, String> config);

    String vendorId();

    // --- DTOs ---

    record LabOrder(
            String orderId,
            PatientRef patient,
            ProviderRef orderingProvider,
            String orgAlias,
            String labId,
            String labAccountNumber,
            List<OrderTest> tests,
            List<String> diagnosisCodes,
            String priority, // ROUTINE, STAT, ASAP
            String collectionType, // PSC, IN_OFFICE, HOME_DRAW, MOBILE_PHLEBOTOMY
            String specimenType,
            boolean fasting,
            String specialInstructions,
            List<AoeAnswer> aoeAnswers,
            Map<String, String> metadata
    ) {}

    record OrderTest(
            String testCode,
            String testName,
            String loincCode,
            List<String> diagnosisPointers
    ) {}

    record AoeAnswer(
            String questionId,
            String answer
    ) {}

    record OrderResult(
            String orderId,
            String status, // SUBMITTED, ACCEPTED, REJECTED, CANCELLED
            String accessionNumber,
            String errorMessage,
            String requisitionUrl
    ) {}

    record OrderStatus(
            String orderId,
            String status, // ORDERED, COLLECTED, IN_TRANSIT, IN_LAB, PARTIAL, COMPLETED, CANCELLED
            String accessionNumber,
            Instant lastUpdated,
            String estimatedCompletion
    ) {}

    record LabResults(
            String orderId,
            String accessionNumber,
            String status, // PRELIMINARY, FINAL, CORRECTED, CANCELLED
            Instant reportDate,
            ProviderRef orderingProvider,
            List<ResultPanel> panels,
            String pdfReportUrl
    ) {}

    record ResultPanel(
            String panelName,
            String loincCode,
            List<ResultItem> items
    ) {}

    record ResultItem(
            String testName,
            String loincCode,
            String value,
            String unit,
            String referenceRange,
            String flag, // NORMAL, HIGH, LOW, CRITICAL_HIGH, CRITICAL_LOW, ABNORMAL
            String status,
            String notes
    ) {}

    record LabTest(
            String testCode,
            String testName,
            String loincCode,
            String specimenType,
            boolean fastingRequired,
            String turnaroundTime,
            List<String> aoeQuestionIds
    ) {}

    record LabFacility(
            String labId,
            String name,
            String address,
            String city,
            String state,
            String zip,
            String phone,
            double distanceMiles,
            List<String> hoursOfOperation,
            boolean walkInAllowed,
            boolean appointmentRequired
    ) {}

    record AoeQuestion(
            String questionId,
            String question,
            String answerType, // TEXT, DATE, BOOLEAN, CHOICE
            List<String> choices,
            boolean required
    ) {}

    record ConnectionStatus(
            boolean connected,
            String message,
            int connectedLabCount
    ) {}
}
