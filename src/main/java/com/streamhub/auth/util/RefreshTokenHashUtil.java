package com.streamhub.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing refresh tokens before storing them in the database.
 *
 * <p><b>Why do we hash refresh tokens?</b></p>
 *
 * <ul>
 *     <li>Never store the raw refresh token in the database.</li>
 *     <li>If the database is compromised, an attacker should NOT obtain usable refresh tokens.</li>
 *     <li>Only the SHA-256 hash is persisted.</li>
 *     <li>The browser keeps the raw refresh token inside an HttpOnly cookie.</li>
 *     <li>Whenever the client sends the refresh token, the server hashes it again
 *         and compares the hash with the stored value.</li>
 * </ul>
 *
 * <p>Authentication Flow:</p>
 *
 * <pre>
 * Browser
 *    │
 *    │ Raw Refresh Token
 *    ▼
 * RefreshTokenHashUtil.hash()
 *    │
 *    ▼
 * SHA-256 Hash
 *    │
 *    ▼
 * PostgreSQL (user_sessions.refresh_token_hash)
 * </pre>
 *
 * <p>This follows the same security principle used for storing passwords:
 * never persist sensitive secrets in plain text.</p>
 */
/**
 * Why NOT Bcrypt?
 * <p>
 * Bcrypt is designed for passwords.
 * <p>
 * Passwords are human-created and usually weak:
 * <p>
 * Password@123
 * Welcome123
 * Admin123
 * <p>
 * Bcrypt is intentionally slow to make brute-force attacks expensive.
 * <p>
 * Refresh tokens are different.
 * <p>
 * They are generated using SecureRandom with 256-bit entropy,
 * making them already cryptographically unpredictable.
 * <p>
 * Since refresh tokens are high-entropy random secrets,
 * there is no practical brute-force attack against them.
 * <p>
 * Using Bcrypt would only make every refresh request slower
 * without providing additional security benefits.
 * <p>
 * Therefore:
 * <p>
 * Passwords      -> Bcrypt
 * Refresh Tokens -> SHA-256
 * <p>
 * This is a common production approach used for hashing
 * randomly generated secrets such as refresh tokens,
 * API keys, password reset tokens, and email verification tokens.
 */
public final class RefreshTokenHashUtil {

    /**
     * Cryptographic hashing algorithm.
     * <p>
     * SHA-256 is:
     * - Fast
     * - Secure
     * - One-way
     * - Built into the Java Standard Library
     * <p>
     * /**
     * Why SHA-256?
     * <p>
     * SHA-256 is a deterministic cryptographic hash function.
     * <p>
     * Deterministic means:
     * <p>
     * Same Input  -> Same Output (Every Time)
     * <p>
     * Example:
     * <p>
     * "abc123"
     * ↓
     * SHA-256
     * ↓
     * e99a18c428cb38d5f260853678922e03...
     * <p>
     * Again:
     * <p>
     * "abc123"
     * ↓
     * SHA-256
     * ↓
     * e99a18c428cb38d5f260853678922e03...
     * <p>
     * The output never changes for the same input.
     * <p>
     * Different Input:
     * <p>
     * "abc124"
     * ↓
     * SHA-256
     * ↓
     * Completely different hash
     * <p>
     * This property allows the server to hash the incoming refresh token
     * during every refresh request and compare it with the hash stored
     * in the database.
     * Refresh tokens are generated using SecureRandom (256-bit entropy),
     * so SHA-256 is sufficient. Unlike passwords, Bcrypt is unnecessary here
     * because refresh tokens are already cryptographically random.
     */
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Utility class.
     * <p>
     * Prevent instantiation because this class contains only static methods.
     */
    private RefreshTokenHashUtil() {
    }

    /**
     * Hashes the raw refresh token using SHA-256.
     * <p>
     * Example:
     * <p>
     * Raw Token:
     * abc123XYZ
     * <p>
     * SHA-256:
     * e0bebd22819993425814866b62701e2919ea26f1370499c0d3f6bdbf1ddc3193
     *
     * @param refreshToken Raw refresh token received from the client
     * @return SHA-256 hexadecimal hash
     */
    public static String hash(String refreshToken) {

        try {

            // Create SHA-256 hashing engine
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

            /*
             * Convert the String into UTF-8 bytes.
             /*
             * Hash functions are deterministic.
             *
             * Every time the same refresh token is hashed,
             * SHA-256 produces exactly the same hash.
             *
             * Example:
             *
             * Refresh Token:
             * AbCd123
             *
             * First Hash:
             * 9f2a7d...
             *
             * Second Hash:
             * 9f2a7d...
             *
             * This allows simple equality comparison with the
             * stored database hash without storing the raw token.
             * Hash algorithms always operate on bytes, never directly on Strings.
             */
            byte[] hashBytes = digest.digest(
                    refreshToken.getBytes(StandardCharsets.UTF_8)
            );

            // Convert binary hash into hexadecimal String
            return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException ex) {

            /*
             * SHA-256 is part of every standard Java runtime.
             *
             * Reaching this block usually indicates a broken or unsupported JVM,
             * so the application cannot continue safely.
             */
            throw new IllegalStateException(
                    "Unable to hash refresh token.",
                    ex
            );
        }
    }

    /**
     * Converts binary hash bytes into a hexadecimal String.
     * <p>
     * Example:
     * <p>
     * byte[]:
     * [15, 127, -21, ...]
     * <p>
     * becomes:
     * <p>
     * 0f7feb...
     * <p>
     * Hexadecimal representation is easier to:
     * - Store in PostgreSQL
     * - Log (if ever required)
     * - Compare
     */
    private static String bytesToHex(byte[] bytes) {

        StringBuilder builder = new StringBuilder();

        for (byte value : bytes) {

            /*
             * %02x
             *
             * %x  -> hexadecimal
             * 02  -> always use two digits
             *
             * Example:
             *
             * 5   -> 05
             * 15  -> 0f
             * 255 -> ff
             */
            builder.append(String.format("%02x", value));
        }

        return builder.toString();
    }
}
