package br.com.deltaglobalbank.identity.domain.user;

public final class ModuleNotEnabledForTenantException extends UserDomainException {

    private final String moduleCode;

    public ModuleNotEnabledForTenantException(String moduleCode) {
        super("module_not_enabled_for_tenant");
        this.moduleCode = moduleCode;
    }

    public String getModuleCode() {
        return moduleCode;
    }
}
