package br.com.deltaglobalbank.identity.domain.ipAllowlist;

public final class IpNotAllowedException extends IpAllowlistExceptions {

    public IpNotAllowedException() {
        super("ip_not_allowed");
    }
}
