package org.ciyex.sdk.config;

import org.ciyex.sdk.files.CiyexFilesClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Auto-configuration for the Ciyex Platform SDK.
 * Provides pre-configured clients for platform services (files, etc.).
 */
@AutoConfiguration
@EnableConfigurationProperties(CiyexPlatformProperties.class)
public class CiyexPlatformAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CiyexFilesClient ciyexFilesClient(CiyexPlatformProperties properties,
                                              RestClient.Builder restClientBuilder) {
        return new CiyexFilesClient(properties.getApiUrl(), restClientBuilder);
    }
}
