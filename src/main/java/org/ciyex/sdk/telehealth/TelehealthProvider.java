package org.ciyex.sdk.telehealth;

import org.ciyex.sdk.common.PatientRef;
import org.ciyex.sdk.common.ProviderRef;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Generic telehealth/video visit provider interface.
 * Vendor adapters (Twilio, Vonage, Zoom, Doxy.me, etc.) implement this.
 */
public interface TelehealthProvider {

    /** Create a new video session room. */
    SessionResult createSession(SessionRequest request);

    /** Generate a join URL/token for a participant. */
    JoinToken generateJoinToken(String sessionId, Participant participant);

    /** End an active session. */
    void endSession(String sessionId);

    /** Get session status and participant info. */
    SessionStatus getSessionStatus(String sessionId);

    /** Get recording URL if recording was enabled. */
    RecordingInfo getRecording(String sessionId);

    /** Verify connectivity. */
    ConnectionStatus testConnection(Map<String, String> config);

    String vendorId();

    // --- DTOs ---

    record SessionRequest(
            String orgAlias,
            String encounterId,
            PatientRef patient,
            ProviderRef provider,
            boolean recordingEnabled,
            boolean waitingRoomEnabled,
            boolean screenShareEnabled,
            int maxDurationMinutes,
            Map<String, String> metadata
    ) {}

    record SessionResult(
            String sessionId,
            String roomName,
            String hostUrl,
            String participantUrl,
            Instant createdAt,
            Instant expiresAt
    ) {}

    record Participant(
            String name,
            String role, // HOST, PARTICIPANT, OBSERVER
            String userId
    ) {}

    record JoinToken(
            String token,
            String joinUrl,
            Instant expiresAt
    ) {}

    record SessionStatus(
            String sessionId,
            String status, // WAITING, ACTIVE, ENDED
            Instant startedAt,
            Instant endedAt,
            int durationSeconds,
            List<String> activeParticipants
    ) {}

    record RecordingInfo(
            String recordingId,
            String sessionId,
            String url,
            String storagePath,
            int durationSeconds,
            long sizeBytes
    ) {}

    record ConnectionStatus(boolean connected, String message) {}
}
