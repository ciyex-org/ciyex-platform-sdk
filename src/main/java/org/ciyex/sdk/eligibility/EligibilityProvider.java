package org.ciyex.sdk.eligibility;

import org.ciyex.sdk.common.Money;
import org.ciyex.sdk.common.PatientRef;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Generic insurance eligibility verification interface.
 * Vendor adapters (pVerify, Availity, Change Healthcare, etc.) implement this.
 *
 * Handles real-time eligibility (EDI 270/271), batch verification,
 * coverage discovery, and patient cost estimation.
 */
public interface EligibilityProvider {

    /**
     * Real-time eligibility check for a single patient.
     */
    EligibilityResponse verifyEligibility(EligibilityRequest request);

    /**
     * Batch eligibility verification for multiple patients.
     */
    List<EligibilityResponse> verifyBatch(List<EligibilityRequest> requests);

    /**
     * Discover unknown insurance coverage for self-pay patients.
     */
    List<CoverageDiscoveryResult> discoverCoverage(PatientRef patient, String orgAlias);

    /**
     * Medicare Beneficiary Identifier (MBI) lookup.
     */
    MbiLookupResult lookupMbi(PatientRef patient);

    /**
     * Estimate patient cost for planned services.
     */
    CostEstimate estimateCost(CostEstimateRequest request);

    /**
     * Check if prior authorization is required for a procedure.
     */
    AuthRequirement checkAuthRequirement(String payerId, String cptCode, String diagnosisCode);

    /**
     * Verify processor connectivity and credentials.
     */
    ConnectionStatus testConnection(Map<String, String> config);

    /** Vendor identifier (e.g., "pverify", "availity", "change-healthcare"). */
    String vendorId();

    // --- DTOs ---

    record EligibilityRequest(
            PatientRef patient,
            String orgAlias,
            String payerId,
            String payerName,
            String memberId,
            String groupNumber,
            String subscriberRelationship,
            LocalDate dateOfService,
            String serviceTypeCode,
            String npi
    ) {}

    record EligibilityResponse(
            String requestId,
            String status, // ACTIVE, INACTIVE, UNKNOWN, ERROR
            PatientRef patient,
            String payerId,
            String payerName,
            String planName,
            String planType, // HMO, PPO, POS, EPO, HDHP
            String memberId,
            String groupNumber,
            String groupName,
            LocalDate effectiveDate,
            LocalDate terminationDate,
            boolean inNetwork,
            String networkName,
            String pcpName,
            String pcpNpi,
            BenefitsSummary benefits,
            List<CopayInfo> copays,
            List<String> alerts,
            boolean referralRequired,
            boolean priorAuthRequired,
            String rawResponse,
            Map<String, Object> vendorData
    ) {}

    record BenefitsSummary(
            // Individual deductible
            Money individualDeductible,
            Money individualDeductibleMet,
            Money individualDeductibleRemaining,
            // Family deductible
            Money familyDeductible,
            Money familyDeductibleMet,
            Money familyDeductibleRemaining,
            // Individual OOP maximum
            Money individualOopMax,
            Money individualOopMet,
            Money individualOopRemaining,
            // Family OOP maximum
            Money familyOopMax,
            Money familyOopMet,
            Money familyOopRemaining,
            // Annual maximums (dental)
            Money annualMaximum,
            Money annualMaximumUsed,
            Money annualMaximumRemaining,
            // Plan dates
            LocalDate planBeginDate,
            LocalDate planEndDate
    ) {}

    record CopayInfo(
            String serviceType, // office_visit, specialist, er, lab, rx_generic, rx_brand
            Money copayAmount,
            String coinsurancePercent,
            String notes
    ) {}

    record CoverageDiscoveryResult(
            PatientRef patient,
            String payerId,
            String payerName,
            String planName,
            String memberId,
            String coverageType, // PRIMARY, SECONDARY, TERTIARY
            String confidence
    ) {}

    record MbiLookupResult(
            String mbi,
            boolean found,
            String partAStatus,
            String partBStatus,
            String partDStatus,
            LocalDate partAEffective,
            LocalDate partBEffective
    ) {}

    record CostEstimateRequest(
            PatientRef patient,
            String payerId,
            String memberId,
            List<ServiceLine> services,
            String orgAlias
    ) {}

    record ServiceLine(
            String cptCode,
            String description,
            String modifier,
            int units,
            Money chargeAmount,
            String diagnosisCode
    ) {}

    record CostEstimate(
            Money totalCharges,
            Money insurancePayment,
            Money patientCopay,
            Money patientDeductible,
            Money patientCoinsurance,
            Money totalPatientResponsibility,
            List<ServiceLineEstimate> lineEstimates
    ) {}

    record ServiceLineEstimate(
            String cptCode,
            Money chargeAmount,
            Money allowedAmount,
            Money insurancePayment,
            Money patientResponsibility,
            String notes
    ) {}

    record AuthRequirement(
            boolean required,
            String cptCode,
            String payerId,
            String turnaroundStandard,
            String turnaroundUrgent,
            List<String> requiredDocuments,
            String submissionMethod // ELECTRONIC, FAX, PHONE, PORTAL
    ) {}

    record ConnectionStatus(
            boolean connected,
            String message,
            int payerCount
    ) {}
}
