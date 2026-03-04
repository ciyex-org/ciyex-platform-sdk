package org.ciyex.sdk.fax;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Generic fax provider interface.
 * Vendor adapters (Documo, SRFax, Phaxio, eFax, etc.) implement this.
 */
public interface FaxProvider {

    /** Send a fax with one or more documents. */
    FaxResult sendFax(FaxRequest request);

    /** Get status of a sent fax. */
    FaxStatus getFaxStatus(String faxId);

    /** List received faxes (inbox). */
    List<ReceivedFax> listInbound(String orgAlias, int limit, int offset);

    /** Download a received fax as PDF. */
    byte[] downloadFax(String faxId);

    /** Provision a fax number for an org. */
    FaxNumber provisionNumber(String orgAlias, String areaCode);

    /** Release a provisioned fax number. */
    void releaseNumber(String faxNumberId);

    /** Verify connectivity. */
    ConnectionStatus testConnection(Map<String, String> config);

    String vendorId();

    // --- DTOs ---

    record FaxRequest(
            String orgAlias,
            String toNumber,
            String fromNumber,
            List<FaxDocument> documents,
            boolean includeCoverPage,
            String coverPageNotes,
            String senderName,
            String recipientName,
            Map<String, String> metadata
    ) {}

    record FaxDocument(
            String filename,
            String contentType,
            byte[] data,
            String storageKey
    ) {}

    record FaxResult(
            String faxId,
            String status, // QUEUED, SENDING, SENT, FAILED
            int pageCount,
            Instant submittedAt
    ) {}

    record FaxStatus(
            String faxId,
            String status, // QUEUED, SENDING, SENT, FAILED, PARTIALLY_SENT
            int pagesSent,
            int totalPages,
            String errorMessage,
            Instant completedAt
    ) {}

    record ReceivedFax(
            String faxId,
            String fromNumber,
            String toNumber,
            int pageCount,
            Instant receivedAt,
            boolean read,
            String callerIdName
    ) {}

    record FaxNumber(
            String faxNumberId,
            String number,
            String orgAlias,
            Instant provisionedAt
    ) {}

    record ConnectionStatus(boolean connected, String message) {}
}
