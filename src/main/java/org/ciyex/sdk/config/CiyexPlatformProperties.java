package org.ciyex.sdk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Ciyex Platform SDK.
 * Marketplace apps configure these to connect to the ciyex-api platform.
 */
@ConfigurationProperties(prefix = "ciyex.platform")
@Getter
@Setter
public class CiyexPlatformProperties {

    /**
     * URL of the ciyex-api gateway (EHR backend).
     * In K8s: http://ciyex-api.ciyex-api.svc.cluster.local:8080
     * Local dev: http://localhost:8080
     */
    private String apiUrl = "http://localhost:8080";
}
