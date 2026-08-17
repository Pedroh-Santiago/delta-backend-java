package br.com.deltaglobalbank.identity.domain.module;

public final class ModuleNotFoundException extends ModuleDomainException {

    public ModuleNotFoundException() {
        super("module_not_found");
    }
}
