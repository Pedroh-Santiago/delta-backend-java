package br.com.deltaglobalbank.identity.domain.ipAllowlist;

public final class InvalidCidrException extends IpAllowlistExceptions {

    public InvalidCidrException() {
        super("invalid_cidr");
    }
}
