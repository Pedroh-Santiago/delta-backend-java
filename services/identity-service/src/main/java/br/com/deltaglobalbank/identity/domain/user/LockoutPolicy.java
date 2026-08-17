package br.com.deltaglobalbank.identity.domain.user;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public record LockoutPolicy(
    boolean enabled,
    List<Integer> attemptsPerLevel,
    List<Duration> durations
) {
    public List<Integer> cumulativeThresholds() {
        List<Integer> thresholds = new ArrayList<>(attemptsPerLevel.size());
        int running = 0;
        for (int attempts : attemptsPerLevel) {
            running += attempts;
            thresholds.add(running);
        }
        return thresholds;
    }
}
