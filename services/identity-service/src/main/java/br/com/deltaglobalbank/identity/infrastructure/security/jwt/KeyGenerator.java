package br.com.deltaglobalbank.identity.infrastructure.security.jwt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class KeyGenerator {

    public KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public String encodePublicKey(PublicKey key) {
        return "-----BEGIN PUBLIC KEY-----\n"
            + chunk(Base64.getEncoder().encodeToString(key.getEncoded()))
            + "\n-----END PUBLIC KEY-----";
    }

    public String encodePrivateKey(PrivateKey key) {
        return "-----BEGIN PRIVATE KEY-----\n"
            + chunk(Base64.getEncoder().encodeToString(key.getEncoded()))
            + "\n-----END PRIVATE KEY-----";
    }

    private String chunk(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i += 64) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(value, i, Math.min(i + 64, value.length()));
        }
        return sb.toString();
    }
}
