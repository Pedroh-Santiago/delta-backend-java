package br.com.deltaglobalbank.identity.domain.module;

public abstract sealed class ModuleDomainException extends RuntimeException
    permits ModuleNotFoundException {

    protected ModuleDomainException(String message) {
        super(message);
    }
}
