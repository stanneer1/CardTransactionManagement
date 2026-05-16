package com.cardtransaction.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Simple HMAC-based token service.
 * Token format: username:timestamp:hexSignature
 * signature = HMAC_SHA256(secret, username + ":" + timestamp)
 */
public final class HmacTokenService {

    private static final String DEFAULT_SECRET = "dev-secret";
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final HexFormat HEX = HexFormat.of();
    // token valid for 5 minutes
    private static final long TTL_SECONDS = 300L;

    private static String secret = System.getenv().getOrDefault("HMAC_SECRET", DEFAULT_SECRET);

    private HmacTokenService() {}

    public static void setSecret(String s) {
        secret = s;
    }

    public static String generateToken(String username) {
        long ts = Instant.now().getEpochSecond();
        String payload = username + ":" + ts;
        String sig = hmacHex(payload);
        return payload + ":" + sig;
    }

    public static boolean validateToken(String token) {
        try {
            if (token == null || token.isBlank()) return false;
            String[] parts = token.split(":");
            if (parts.length != 3) return false;
            String username = parts[0];
            long ts = Long.parseLong(parts[1]);
            String sig = parts[2];
            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - ts) > TTL_SECONDS) return false;
            String payload = username + ":" + ts;
            String expected = hmacHex(payload);
            return constantTimeEquals(expected, sig);
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractUsername(String token) {
        if (token == null) return null;
        String[] parts = token.split(":");
        if (parts.length != 3) return null;
        return parts[0];
    }

    private static String hmacHex(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(keySpec);
            byte[] sig = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(sig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

