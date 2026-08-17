package br.com.deltaglobalbank.identity.infrastructure.security.apikey;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

@Component
public class ApiKeyFingerprinter {

    private static final String ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKey;

    public ApiKeyFingerprinter(ApiKeyProperties properties) {
        this.secretKey = new SecretKeySpec(
            properties.fingerprintSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    public String fingerprint(String plainKey) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKey);
            byte[] bytes = mac.doFinal(plainKey.getBytes(StandardCharsets.UTF_8));
            return toHexString(bytes);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
