package br.com.deltaglobalbank.identity.infrastructure.security.apikey;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class ApiKeyGenerator {

    public static final int KEY_RANDOM_LENGTH = 32;
    public static final int PREFIX_LENGTH = 13;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final ApiKeyProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyGenerator(ApiKeyProperties properties) {
        this.properties = properties;
    }

    public GeneratedApiKey generate() {
        StringBuilder random = new StringBuilder(KEY_RANDOM_LENGTH);
        for (int i = 0; i < KEY_RANDOM_LENGTH; i++) {
            random.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        String plainKey = properties.brand() + "_" + properties.environment() + "_" + random;
        String prefix = plainKey.substring(0, PREFIX_LENGTH);

        return new GeneratedApiKey(plainKey, prefix);
    }
}
