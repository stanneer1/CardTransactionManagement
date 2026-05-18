package com.cardtransaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for token issuance
 */
public class TokenResponse {

    private String token;

    @JsonProperty("expiresAt")
    private String expiresAt;

    @JsonProperty("ttlSeconds")
    private long ttlSeconds;

    public TokenResponse(String token, String expiresAt, long ttlSeconds) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.ttlSeconds = ttlSeconds;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}

