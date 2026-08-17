package br.com.deltaglobalbank.identity.infrastructure.security.password;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class TemporaryPasswordGeneratorTests {

    private final TemporaryPasswordGenerator generator = new TemporaryPasswordGenerator();

    @Test
    void generatedPasswordMustHaveDefaultLengthOf16() {
        String password = generator.generatePassword();
        assertEquals(16, password.length());
    }

    @Test
    void generatedPasswordMustRespectCustomLength() {
        String password = generator.generatePassword(24);
        assertEquals(24, password.length());
    }

    @Test
    void generatedPasswordMustContainAtLeastOneDigit() {
        for (int i = 0; i < 100; i++) {
            String password = generator.generatePassword();
            assertTrue(password.chars().anyMatch(Character::isDigit), "Password without digit: " + password);
        }
    }

    @Test
    void generatedPasswordMustOnlyContainAllowedCharacters() {
        Set<Character> allowed = "23456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".chars()
            .mapToObj(c -> (char) c)
            .collect(java.util.stream.Collectors.toSet());
        for (int i = 0; i < 100; i++) {
            String password = generator.generatePassword();
            assertTrue(password.chars().allMatch(c -> allowed.contains((char) c)), "Password with invalid char: " + password);
        }
    }
}
