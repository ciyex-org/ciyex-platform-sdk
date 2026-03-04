package org.ciyex.sdk.common;

/**
 * Standard address record used across SDKs.
 */
public record Address(
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country
) {
    public Address {
        if (country == null || country.isBlank()) country = "US";
    }
}
