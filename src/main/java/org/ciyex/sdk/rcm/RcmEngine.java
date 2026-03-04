package org.ciyex.sdk.rcm;

import org.ciyex.sdk.common.Money;
import org.ciyex.sdk.common.PatientRef;
import org.ciyex.sdk.common.ProviderRef;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Generic Revenue Cycle Management engine interface.
 * Vendor adapters (Waystar, Trizetto, Change Healthcare, etc.) implement this.
 *
 * Covers claim submission, scrubbing, denial management, payment posting,
 * and financial analytics.
 */
public interface RcmEngine {

    // --- Claim Lifecycle ---

    /**
     * Scrub a claim against NCCI, MUE, LCD/NCD, and payer-specific rules.
     */
    ClaimScrubResult scrubClaim(Claim claim);

    /**
     * Submit a claim electronically (EDI 837).
     */
    ClaimSubmitResult submitClaim(Claim claim);

    /**
     * Submit a batch of claims.
     */
    List<ClaimSubmitResult> submitBatch(List<Claim> claims);

    /**
     * Check claim status (EDI 276/277).
     */
    ClaimStatusResult checkStatus(String claimId, String payerId);

    /**
     * Resubmit a corrected claim.
     */
    ClaimSubmitResult resubmitClaim(String originalClaimId, Claim correctedClaim);

    // --- Payment Posting ---

    /**
     * Parse an ERA/835 remittance.
     */
    EraParseResult parseEra(String rawEra);

    /**
     * Auto-post payments from a parsed ERA.
     */
    List<PostingResult> autoPostPayments(EraParseResult era);

    // --- Denial Management ---

    /**
     * Analyze a denial and suggest corrective action.
     */
    DenialAnalysis analyzeDenial(DenialInfo denial);

    /**
     * Generate an appeal letter using AI/templates.
     */
    AppealLetter generateAppeal(DenialInfo denial, List<String> supportingDocIds);

    // --- Coding ---

    /**
     * Suggest CPT/ICD codes from clinical note text (AI-assisted coding).
     */
    CodingSuggestion suggestCodes(String clinicalNoteText, String encounterType);

    // --- Contract & Underpayment ---

    /**
     * Check expected reimbursement against a payer contract.
     */
    ReimbursementCheck checkReimbursement(String payerId, String cptCode, Money paidAmount);

    /**
     * Verify connectivity with clearinghouse.
     */
    ConnectionStatus testConnection(Map<String, String> config);

    /** Vendor identifier (e.g., "waystar", "trizetto", "change-hc"). */
    String vendorId();

    // --- DTOs ---

    record Claim(
            String claimId,
            String claimType, // PROFESSIONAL, INSTITUTIONAL, DENTAL
            PatientRef patient,
            ProviderRef renderingProvider,
            ProviderRef billingProvider,
            String facilityNpi,
            String payerId,
            String payerName,
            String subscriberId,
            String groupNumber,
            LocalDate dateOfService,
            LocalDate dateOfServiceEnd,
            String placeOfService,
            List<ClaimLine> lines,
            List<String> diagnosisCodes,
            String priorAuthNumber,
            String referringProviderNpi,
            Money totalCharges,
            Map<String, String> metadata
    ) {}

    record ClaimLine(
            int lineNumber,
            String cptCode,
            List<String> modifiers,
            int units,
            Money chargeAmount,
            List<String> diagnosisPointers,
            String revenueCode,
            String ndcCode
    ) {}

    record ClaimScrubResult(
            boolean clean,
            List<ClaimEdit> edits
    ) {}

    record ClaimEdit(
            String editType, // NCCI, MUE, LCD, NCD, PAYER_SPECIFIC, MODIFIER, DIAGNOSIS
            String severity, // ERROR, WARNING, INFO
            String code,
            String message,
            int lineNumber,
            String suggestedFix
    ) {}

    record ClaimSubmitResult(
            String claimId,
            String trackingNumber,
            String status, // ACCEPTED, REJECTED, PENDING
            List<String> rejectionReasons,
            String submissionDate
    ) {}

    record ClaimStatusResult(
            String claimId,
            String status, // PENDING, ADJUDICATED, PAID, DENIED, PARTIAL
            String statusDate,
            Money paidAmount,
            Money patientResponsibility,
            String checkEftNumber,
            List<String> statusDetails
    ) {}

    record EraParseResult(
            String eraId,
            String payerId,
            String payerName,
            String checkEftNumber,
            Money totalPaid,
            LocalDate paymentDate,
            List<EraClaimDetail> claims
    ) {}

    record EraClaimDetail(
            String claimId,
            String patientName,
            Money billed,
            Money allowed,
            Money paid,
            Money patientResponsibility,
            List<EraLineDetail> lines,
            List<AdjustmentDetail> adjustments
    ) {}

    record EraLineDetail(
            String cptCode,
            Money billed,
            Money allowed,
            Money paid,
            List<AdjustmentDetail> adjustments
    ) {}

    record AdjustmentDetail(
            String groupCode, // CO, PR, OA, PI, CR
            String reasonCode, // CARC
            String remarkCode, // RARC
            Money amount,
            String description
    ) {}

    record PostingResult(
            String claimId,
            boolean posted,
            Money insurancePayment,
            Money contractualAdjustment,
            Money patientBalance,
            boolean underpaymentDetected,
            Money underpaymentAmount,
            String errorMessage
    ) {}

    record DenialInfo(
            String claimId,
            String payerId,
            String carcCode,
            String rarcCode,
            String groupCode,
            Money deniedAmount,
            String denialDate,
            String timelyFilingDeadline,
            String originalCptCode,
            String originalDiagnosisCode,
            PatientRef patient
    ) {}

    record DenialAnalysis(
            String claimId,
            String rootCause,
            String category, // CLINICAL, ADMINISTRATIVE, TECHNICAL, ELIGIBILITY
            String recommendedAction, // RESUBMIT, APPEAL, CORRECT_AND_RESUBMIT, WRITE_OFF
            double recoveryProbability,
            String suggestedCorrection,
            int priority // 1-5 (1=highest)
    ) {}

    record AppealLetter(
            String claimId,
            String letterText,
            List<String> requiredAttachments,
            String submissionMethod, // ELECTRONIC, FAX, MAIL
            String payerAppealAddress
    ) {}

    record CodingSuggestion(
            List<CodeSuggestion> diagnosisCodes,
            List<CodeSuggestion> procedureCodes,
            List<String> cdiAlerts
    ) {}

    record CodeSuggestion(
            String code,
            String description,
            double confidence,
            String evidence
    ) {}

    record ReimbursementCheck(
            String cptCode,
            String payerId,
            Money expectedAmount,
            Money actualPaid,
            Money variance,
            boolean underpaid
    ) {}

    record ConnectionStatus(
            boolean connected,
            String message,
            String clearinghouseName,
            int payerConnectionCount
    ) {}
}
