package br.com.deltaglobalbank.identity.infrastructure.security.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class RefreshTokenGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public GeneratedRefreshToken generate() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String plainText = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = sha256(plainText);
        return new GeneratedRefreshToken(plainText, hash);
    }

    public String hash(String plainText) {
        return sha256(plainText);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
