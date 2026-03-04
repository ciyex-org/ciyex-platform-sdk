package org.ciyex.sdk.erx;

import org.ciyex.sdk.common.PatientRef;
import org.ciyex.sdk.common.ProviderRef;

import java.util.List;
import java.util.Map;

/**
 * Generic e-prescribing provider interface.
 * Vendor adapters (DoseSpot, DrFirst/Rcopia, NewCrop, etc.) implement this.
 * All vendors route through Surescripts network.
 */
public interface ErxProvider {

    /** Send a new prescription to a pharmacy. */
    PrescriptionResult prescribe(PrescriptionRequest request);

    /** Cancel a previously sent prescription. */
    PrescriptionResult cancelPrescription(String prescriptionId, String reason);

    /** Process a refill request from pharmacy. */
    PrescriptionResult respondToRefill(String refillRequestId, String action, String note);

    /** Search for pharmacies by location. */
    List<Pharmacy> searchPharmacies(String query, String zipCode, int radiusMiles);

    /** Search medication database. */
    List<Medication> searchMedications(String query, int limit);

    /** Check drug interactions for a medication list. */
    InteractionCheckResult checkInteractions(List<String> rxcuis, PatientRef patient);

    /** Get formulary/benefit info for a medication. */
    FormularyResult checkFormulary(String rxcui, String payerId, String memberId);

    /** Get patient medication history (Surescripts MedHistory). */
    List<MedicationHistory> getMedicationHistory(PatientRef patient, ProviderRef provider);

    /** Query state PDMP (Prescription Drug Monitoring Program). */
    PdmpResult queryPdmp(PatientRef patient, ProviderRef provider, String state);

    /** Enroll a provider for e-prescribing. */
    EnrollmentResult enrollProvider(ProviderRef provider, String orgAlias);

    /** Verify connectivity and Surescripts certification status. */
    ConnectionStatus testConnection(Map<String, String> config);

    String vendorId();

    // --- DTOs ---

    record PrescriptionRequest(
            PatientRef patient,
            ProviderRef provider,
            String orgAlias,
            String rxcui,
            String drugName,
            String drugStrength,
            String drugForm,
            String sig, // directions
            int quantity,
            String quantityUnit,
            int daysSupply,
            int refills,
            String dawCode, // Dispense As Written
            String pharmacyNcpdpId,
            String diagnosisCode,
            String notes,
            boolean isControlled,
            String scheduleClass, // II, III, IV, V
            Map<String, String> metadata
    ) {}

    record PrescriptionResult(
            String prescriptionId,
            String status, // SENT, ACCEPTED, REJECTED, CANCELLED, ERROR
            String pharmacyName,
            String errorMessage,
            String surescriptsMessageId
    ) {}

    record Pharmacy(
            String ncpdpId,
            String name,
            String address,
            String city,
            String state,
            String zip,
            String phone,
            String fax,
            double distanceMiles,
            boolean acceptsEpcs,
            boolean mailOrder,
            boolean specialtyPharmacy,
            boolean is24Hour
    ) {}

    record Medication(
            String rxcui,
            String name,
            String strength,
            String form,
            String route,
            boolean isGeneric,
            boolean isControlled,
            String scheduleClass,
            String ndc
    ) {}

    record InteractionCheckResult(
            boolean hasInteractions,
            List<Interaction> interactions
    ) {}

    record Interaction(
            String severity, // CONTRAINDICATION, SEVERE, MODERATE, MILD
            String drug1,
            String drug2,
            String description,
            String clinicalEffect
    ) {}

    record FormularyResult(
            String drugName,
            String formularyStatus, // ON_FORMULARY, NOT_COVERED, PRIOR_AUTH_REQUIRED
            int tier,
            String copayAmount,
            boolean priorAuthRequired,
            boolean stepTherapyRequired,
            boolean quantityLimitApplies,
            List<String> alternatives
    ) {}

    record MedicationHistory(
            String drugName,
            String strength,
            String prescriber,
            String pharmacy,
            String lastFillDate,
            int daysSupply,
            String status
    ) {}

    record PdmpResult(
            boolean found,
            List<PdmpRecord> records,
            String queryDate,
            String state
    ) {}

    record PdmpRecord(
            String drugName,
            String schedule,
            String prescriber,
            String pharmacy,
            String dispensedDate,
            int quantity,
            int daysSupply
    ) {}

    record EnrollmentResult(
            String providerId,
            String status, // PENDING, ENROLLED, REJECTED
            String surescriptsId,
            boolean epcsEnabled,
            String errorMessage
    ) {}

    record ConnectionStatus(
            boolean connected,
            String message,
            String certificationStatus,
            String environment
    ) {}
}
