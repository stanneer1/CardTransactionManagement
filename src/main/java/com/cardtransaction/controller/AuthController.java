package com.cardtransaction.controller;

import com.cardtransaction.dto.TokenResponse;
import com.cardtransaction.security.HmacTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Authentication controller for issuing HMAC tokens.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Issue an HMAC token for the authenticated user.
     * This endpoint requires Basic authentication.
     *
     * POST /api/auth/token
     *
     * Example:
     *   curl -u user:user123 -X POST http://localhost:8080/api/auth/token
     *
     * Response example:
     *   {
     *     "token": "user:1234567890:abc123...",
     *     "expiresAt": "2026-05-18T14:00:00Z",
     *     "ttlSeconds": 3600
     *   }
     *
     * @return TokenResponse with the generated token and expiry time
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> issueToken() {
        // Get the authenticated user from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = auth.getName();
        String token = HmacTokenService.generateToken(username);
        String expiryTime = HmacTokenService.getTokenExpiryTimestamp(token);
        long ttl = HmacTokenService.getTtlSeconds();

        TokenResponse response = new TokenResponse(token, expiryTime, ttl);
        return ResponseEntity.ok(response);
    }
}

