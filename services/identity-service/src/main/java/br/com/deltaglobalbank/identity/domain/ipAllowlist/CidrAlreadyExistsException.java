package br.com.deltaglobalbank.identity.domain.ipAllowlist;

public final class CidrAlreadyExistsException extends IpAllowlistExceptions {

    public CidrAlreadyExistsException() {
        super("cidr_already_exists");
    }
}
