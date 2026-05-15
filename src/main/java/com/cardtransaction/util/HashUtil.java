package com.cardtransaction.util;

import com.cardtransaction.dto.PurchaseTransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utility class for generating SHA-256 hashes for objects and strings.
 * Provides generic methods for hashing any object (via JSON serialization) or raw strings.
 *
 * This utility is designed to be reusable across different use cases:
 * - Duplicate detection (transaction records, user data, etc.)
 * - Data integrity verification
 * - Change detection
 * - Deduplication algorithms
 */
public class HashUtil {

    private static final Logger logger = LoggerFactory.getLogger(HashUtil.class);
    private static final String ALGORITHM = "SHA-256";
    private static final ObjectMapper objectMapper;
    private static final HexFormat hexFormat = HexFormat.of();

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private HashUtil() {
    }

    /**
     * Generic method to generate SHA-256 hash for any object.
     * Converts the object to JSON and generates a hash from its JSON representation.
     * Useful for hashing DTOs, requests, entities, and any serializable objects.
     *
     * @param <T> the type of object to hash
     * @param object the object to hash (must be JSON serializable)
     * @return SHA-256 hash as a 64-character hexadecimal string
     * @throws RuntimeException if hash generation fails
     *
     * @example
     * <pre>
     * PurchaseTransactionRequest request = new PurchaseTransactionRequest(...);
     * String hash = HashUtil.generateHash(request);
     *
     * User user = new User(...);
     * String hash = HashUtil.generateHash(user);
     * </pre>
     */
    public static <T> String generateHash(T object) {
        try {
            String jsonString = objectMapper.writeValueAsString(object);
            logger.debug("Generating hash for object: {}", jsonString);
            return hashBytes(jsonString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Error generating hash for object", e);
            throw new RuntimeException("Error generating hash for object", e);
        }
    }

    /**
     * Generate SHA-256 hash for a raw string.
     * Useful for hashing plain strings, concatenated values, or custom formatted data.
     *
     * @param input the string to hash
     * @return SHA-256 hash as a 64-character hexadecimal string
     * @throws RuntimeException if hash generation fails
     *
     * @example
     * <pre>
     * String data = "description|date|amount";
     * String hash = HashUtil.hashString(data);
     *
     * String combined = user.getId() + user.getEmail();
     * String hash = HashUtil.hashString(combined);
     * </pre>
     */
    public static String hashString(String input) {
        try {
            if (input == null || input.isEmpty()) {
                throw new IllegalArgumentException("Input string cannot be null or empty");
            }
            logger.debug("Generating hash for string: {}", input);
            return hashBytes(input.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for hash generation", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error generating hash for string", e);
            throw new RuntimeException("Error generating hash for string", e);
        }
    }

    /**
     * Generate SHA-256 hash for raw bytes.
     * Useful for hashing binary data or pre-processed byte arrays.
     *
     * @param bytes the byte array to hash
     * @return SHA-256 hash as a 64-character hexadecimal string
     * @throws RuntimeException if hash generation fails
     *
     * @example
     * <pre>
     * byte[] data = "some data".getBytes(StandardCharsets.UTF_8);
     * String hash = HashUtil.hashBytes(data);
     * </pre>
     */
    public static String hashBytes(byte[] bytes) {
        try {
            if (bytes == null || bytes.length == 0) {
                throw new IllegalArgumentException("Byte array cannot be null or empty");
            }
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = messageDigest.digest(bytes);
            return hexFormat.formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Error generating hash: SHA-256 algorithm not available", e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for hash generation", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error generating hash for bytes", e);
            throw new RuntimeException("Error generating hash for bytes", e);
        }
    }

    /**
     * Convenience method for backward compatibility.
     * Generates SHA-256 hash for PurchaseTransactionRequest.
     *
     * @param request the purchase transaction request
     * @return SHA-256 hash as a 64-character hexadecimal string
     */
    public static String generateHash(PurchaseTransactionRequest request) {
        return generateHash((Object) request);
    }

    /**
     * Check if two objects produce the same hash.
     * Useful for detecting if objects have identical content.
     *
     * @param <T> the type of objects to compare
     * @param object1 the first object
     * @param object2 the second object
     * @return true if both objects produce the same hash, false otherwise
     *
     * @example
     * <pre>
     * PurchaseTransactionRequest req1 = new PurchaseTransactionRequest(...);
     * PurchaseTransactionRequest req2 = new PurchaseTransactionRequest(...);
     *
     * if (HashUtil.isSameHash(req1, req2)) {
     *     // Both requests have identical content
     * }
     * </pre>
     */
    public static <T> boolean isSameHash(T object1, T object2) {
        try {
            String hash1 = generateHash(object1);
            String hash2 = generateHash(object2);
            return hash1.equals(hash2);
        } catch (Exception e) {
            logger.error("Error comparing hashes", e);
            return false;
        }
    }

    /**
     * Get the hash algorithm used by this utility.
     *
     * @return the algorithm name (e.g., "SHA-256")
     */
    public static String getAlgorithm() {
        return ALGORITHM;
    }

    /**
     * Get the hash length in characters.
     * SHA-256 produces a 64-character hexadecimal string.
     *
     * @return the hash length in characters
     */
    public static int getHashLength() {
        return 64;
    }
}



