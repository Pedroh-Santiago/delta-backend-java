package br.com.deltaglobalbank.identity.domain.ipAllowlist;

public final class IpAllowlistEntryNotFoundException extends IpAllowlistExceptions {

    public IpAllowlistEntryNotFoundException() {
        super("ip_allowlist_entry_not_found");
    }
}
