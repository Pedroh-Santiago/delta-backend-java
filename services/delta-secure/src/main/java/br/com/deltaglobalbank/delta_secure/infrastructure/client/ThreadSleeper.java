package br.com.deltaglobalbank.delta_secure.infrastructure.client;

import org.springframework.stereotype.Component;

@Component
public class ThreadSleeper implements Sleeper {

    @Override
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
