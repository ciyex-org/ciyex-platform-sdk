package org.ciyex.sdk.common;

/**
 * Base exception for all SDK operations.
 * Vendor adapters should wrap vendor-specific errors in this.
 */
public class SdkException extends RuntimeException {

    private final String errorCode;
    private final String vendorCode;

    public SdkException(String message) {
        this(message, null, null, null);
    }

    public SdkException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public SdkException(String message, String errorCode, String vendorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.vendorCode = vendorCode;
    }

    public String getErrorCode() { return errorCode; }
    public String getVendorCode() { return vendorCode; }
}
