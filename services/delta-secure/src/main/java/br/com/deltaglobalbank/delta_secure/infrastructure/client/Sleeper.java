package br.com.deltaglobalbank.delta_secure.infrastructure.client;

@FunctionalInterface
public interface Sleeper {
    void sleep(long millis);
}
