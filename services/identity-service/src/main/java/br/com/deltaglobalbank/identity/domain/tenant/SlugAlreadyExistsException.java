package br.com.deltaglobalbank.identity.domain.tenant;

public final class SlugAlreadyExistsException extends TenantDomainException {

    public SlugAlreadyExistsException() {
        super("slug_already_exists");
    }
}
