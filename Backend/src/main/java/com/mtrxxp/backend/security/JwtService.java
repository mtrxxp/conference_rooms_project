package com.mtrxxp.backend.security;

import com.mtrxxp.backend.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtService {

    private static final Pattern SUB = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern EXP = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");

    private final byte[] secret;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret:change-me-please-use-a-32+char-secret-key}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        long nowSeconds = System.currentTimeMillis() / 1000;
        long expSeconds = nowSeconds + expirationMs / 1000;

        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode(String.format(
                "{\"sub\":\"%s\",\"uid\":%d,\"role\":\"%s\",\"iat\":%d,\"exp\":%d}",
                user.getEmail(), user.getId(), user.getRole(), nowSeconds, expSeconds));

        String signingInput = header + "." + payload;
        return signingInput + "." + sign(signingInput);
    }

    public Optional<String> validateAndGetSubject(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        String signingInput = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(signingInput), parts[2])) {
            return Optional.empty();
        }

        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        Matcher expMatcher = EXP.matcher(payload);
        if (expMatcher.find() && Long.parseLong(expMatcher.group(1)) < System.currentTimeMillis() / 1000) {
            return Optional.empty();
        }

        Matcher subMatcher = SUB.matcher(payload);
        return subMatcher.find() ? Optional.of(subMatcher.group(1)) : Optional.empty();
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(String json) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
