package org.ciyex.sdk.ai;

import java.util.List;
import java.util.Map;

/**
 * Generic clinical AI assistant interface.
 * Vendor adapters (ambient AI, clinical decision support, etc.) implement this.
 */
public interface ClinicalAiProvider {

    /** Transcribe audio to text (ambient documentation). */
    TranscriptionResult transcribe(TranscriptionRequest request);

    /** Generate structured clinical note from transcript. */
    ClinicalNote generateNote(NoteRequest request);

    /** Generate coding suggestions from clinical note. */
    List<CodeSuggestion> suggestCodes(String clinicalNoteText, String encounterType);

    /** Clinical decision support query. */
    CdsResult queryCds(CdsRequest request);

    /** Summarize patient history. */
    String summarizePatient(String patientId, String orgAlias, String scope);

    /** Verify connectivity. */
    ConnectionStatus testConnection(Map<String, String> config);

    String vendorId();

    // --- DTOs ---

    record TranscriptionRequest(
            String sessionId,
            String audioFormat, // WAV, MP3, WEBM, OGG
            byte[] audioData,
            String audioUrl,
            String language,
            boolean speakerDiarization
    ) {}

    record TranscriptionResult(
            String transcriptId,
            String text,
            List<SpeakerSegment> segments,
            double confidenceScore,
            int durationSeconds
    ) {}

    record SpeakerSegment(
            String speaker, // PROVIDER, PATIENT, UNKNOWN
            String text,
            double startSeconds,
            double endSeconds
    ) {}

    record NoteRequest(
            String transcriptText,
            String encounterType, // OFFICE_VISIT, TELEHEALTH, PHONE
            String specialty,
            String templateFormat, // SOAP, HISTORY_PHYSICAL, PROGRESS
            String patientContext
    ) {}

    record ClinicalNote(
            String noteId,
            String format, // SOAP, HISTORY_PHYSICAL, PROGRESS
            String subjective,
            String objective,
            String assessment,
            String plan,
            String fullText,
            List<CodeSuggestion> suggestedCodes,
            double confidenceScore
    ) {}

    record CodeSuggestion(
            String codeSystem, // ICD10, CPT, SNOMED
            String code,
            String description,
            double confidence,
            String evidence
    ) {}

    record CdsRequest(
            String query,
            String patientAge,
            String patientSex,
            List<String> diagnoses,
            List<String> medications,
            List<String> allergies,
            String clinicalContext
    ) {}

    record CdsResult(
            String response,
            List<String> references,
            List<Alert> alerts
    ) {}

    record Alert(
            String severity, // INFO, WARNING, CRITICAL
            String category, // DRUG_INTERACTION, ALLERGY, GUIDELINE, PREVENTIVE
            String message,
            String evidence
    ) {}

    record ConnectionStatus(boolean connected, String message, String modelVersion) {}
}
