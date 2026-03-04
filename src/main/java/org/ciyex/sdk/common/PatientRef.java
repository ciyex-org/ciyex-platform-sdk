package org.ciyex.sdk.common;

/**
 * Lightweight patient reference passed between services.
 * Does not contain PHI — only identifiers needed for correlation.
 */
public record PatientRef(
        String patientId,
        String orgAlias,
        String mrn,
        String firstName,
        String lastName,
        String dateOfBirth
) {}
