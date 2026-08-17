package br.com.deltaglobalbank.identity.infrastructure.security.lockout;

import java.time.Duration;
import java.util.List;

import br.com.deltaglobalbank.identity.domain.user.LockoutPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "identity.security.lockout")
public record LockoutProperties(
    @DefaultValue("true") boolean enabled,
    @DefaultValue({"5", "3", "2"}) List<Integer> attemptsPerLevel,
    @DefaultValue({"PT5M", "PT15M"}) List<Duration> durations
) {
    public LockoutProperties {
        if (attemptsPerLevel.isEmpty()) {
            throw new IllegalArgumentException("attempts-per-level não pode ser vazio");
        }
        if (attemptsPerLevel.stream().anyMatch(attempts -> attempts < 1)) {
            throw new IllegalArgumentException("cada attempts-per-level deve ser >= 1");
        }
        if (durations.size() != attemptsPerLevel.size() - 1) {
            throw new IllegalArgumentException(
                "durations deve ter (attempts-per-level - 1) itens — o último nível é sempre permanente"
            );
        }
        if (durations.stream().anyMatch(duration -> duration.isNegative() || duration.isZero())) {
            throw new IllegalArgumentException("durations devem ser > 0");
        }
    }

    public LockoutPolicy toPolicy() {
        return new LockoutPolicy(enabled, attemptsPerLevel, durations);
    }
}
