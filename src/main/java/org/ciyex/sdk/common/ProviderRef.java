package org.ciyex.sdk.common;

/**
 * Lightweight provider/practitioner reference.
 */
public record ProviderRef(
        String providerId,
        String npi,
        String name,
        String specialty
) {}
