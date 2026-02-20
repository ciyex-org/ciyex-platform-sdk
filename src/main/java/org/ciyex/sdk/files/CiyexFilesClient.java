package org.ciyex.sdk.files;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.util.Map;

/**
 * Client for the Ciyex Platform file storage API.
 * Routes through ciyex-api's /api/files-proxy endpoints which handle:
 * - Vaultik (ciyex-files service) when storage_mode = "files-service"
 * - Local filesystem when storage_mode = "local"
 *
 * Marketplace apps should inject this bean and use it for all file operations
 * instead of calling ciyex-files directly.
 *
 * Usage:
 * <pre>
 * &#64;Autowired CiyexFilesClient filesClient;
 *
 * // Upload
 * filesClient.uploadBytes(data, "video/mp4", "recordings/org1/session123/video.mp4",
 *                         "org1", "telehealth", "session123", "video.mp4");
 *
 * // Get presigned URL
 * String url = filesClient.getPresignedUrl("recordings/org1/session123/video.mp4", 3600);
 *
 * // Download
 * byte[] data = filesClient.download("recordings/org1/session123/video.mp4");
 * </pre>
 */
@Slf4j
public class CiyexFilesClient {

    private final RestClient restClient;

    public CiyexFilesClient(String apiUrl, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(apiUrl.replaceAll("/$", ""))
                .build();
    }

    /**
     * Upload raw bytes to a specific key path.
     */
    public void uploadBytes(byte[] data, String contentType, String key,
                            String orgId, String sourceService, String referenceId,
                            String originalFilename) {
        var spec = restClient.post()
                .uri("/api/files-proxy/store-bytes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken())
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header("X-File-Path", key)
                .header("X-Source-Service", sourceService != null ? sourceService : "unknown")
                .header("X-Org-Id", orgId != null ? orgId : "");

        if (referenceId != null) spec.header("X-Reference-Id", referenceId);
        if (originalFilename != null) spec.header("X-Original-Filename", originalFilename);

        spec.body(data).retrieve().toBodilessEntity();
        log.debug("Uploaded bytes via platform: key={}", key);
    }

    /**
     * Upload an InputStream (reads to bytes first).
     */
    public void uploadStream(InputStream inputStream, long contentLength, String contentType,
                             String key, String orgId, String sourceService,
                             String referenceId, String originalFilename) {
        try {
            byte[] data = inputStream.readAllBytes();
            uploadBytes(data, contentType, key, orgId, sourceService, referenceId, originalFilename);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload stream via platform: " + key, e);
        }
    }

    /**
     * Generate a presigned download URL for a key.
     */
    @SuppressWarnings("unchecked")
    public String getPresignedUrl(String key, int expirySeconds) {
        Map<String, Object> response = restClient.get()
                .uri("/api/files-proxy/by-key/presigned-url?key={key}&expiry={expiry}", key, expirySeconds)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken())
                .retrieve()
                .body(Map.class);

        if (response != null) {
            Object data = response.get("data");
            if (data instanceof Map) {
                return (String) ((Map<?, ?>) data).get("url");
            }
            return (String) response.get("url");
        }
        return null;
    }

    /**
     * Check if a file exists at the given key.
     */
    public boolean exists(String key) {
        try {
            restClient.head()
                    .uri("/api/files-proxy/by-key/exists?key={key}", key)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken())
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the size of a file at the given key.
     */
    @SuppressWarnings("unchecked")
    public long getObjectSize(String key) {
        Map<String, Object> response = restClient.get()
                .uri("/api/files-proxy/by-key/size?key={key}", key)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken())
                .retrieve()
                .body(Map.class);

        if (response != null) {
            Object data = response.get("data");
            if (data instanceof Map) {
                Object size = ((Map<?, ?>) data).get("size");
                return size != null ? ((Number) size).longValue() : 0;
            }
            Object size = response.get("size");
            return size != null ? ((Number) size).longValue() : 0;
        }
        return 0;
    }

    /**
     * Delete a file by key.
     */
    public void delete(String key) {
        restClient.delete()
                .uri("/api/files-proxy/by-key?key={key}", key)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken())
                .retrieve()
                .toBodilessEntity();
        log.debug("Deleted via platform: key={}", key);
    }

    /**
     * Download file bytes by key.
     */
    public byte[] download(String key) {
        return restClient.get()
                .uri("/api/files-proxy/by-key/download?key={key}", key)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAuthToken())
                .retrieve()
                .body(byte[].class);
    }

    private String getAuthToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        log.warn("No JWT available for platform API call");
        return "";
    }
}
