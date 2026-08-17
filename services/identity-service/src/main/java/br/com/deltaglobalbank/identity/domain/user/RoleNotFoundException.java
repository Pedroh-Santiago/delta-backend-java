package br.com.deltaglobalbank.identity.domain.user;

public final class RoleNotFoundException extends UserDomainException {

    private final String roleCode;

    public RoleNotFoundException(String roleCode) {
        super("role_not_found");
        this.roleCode = roleCode;
    }

    public String getRoleCode() {
        return roleCode;
    }
}
