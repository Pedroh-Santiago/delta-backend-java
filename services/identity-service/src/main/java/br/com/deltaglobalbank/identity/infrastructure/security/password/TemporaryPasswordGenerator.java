package br.com.deltaglobalbank.identity.infrastructure.security.password;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TemporaryPasswordGenerator {

    private static final String DIGITS = "23456789";
    private static final String LETTERS = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String ALL = DIGITS + LETTERS;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generatePassword() {
        return generatePassword(16);
    }

    public String generatePassword(int length) {
        List<Character> chars = new ArrayList<>(length);
        chars.add(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
        for (int i = 1; i < length; i++) {
            chars.add(ALL.charAt(secureRandom.nextInt(ALL.length())));
        }
        Collections.shuffle(chars, secureRandom);

        StringBuilder sb = new StringBuilder(length);
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }
}
