package br.com.deltaglobalbank.identity.domain.ipAllowlist;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

public record Cidr(String value) {

    public Cidr {
        IPAddressString parsed = new IPAddressString(value);
        if (value.isBlank() || !parsed.isValid() || !parsed.isPrefixed()) {
            throw new InvalidCidrException();
        }
    }

    public boolean matches(String ip) {
        IPAddress network = new IPAddressString(value).getAddress();
        if (network == null) {
            return false;
        }
        IPAddress ipAddr;
        try {
            ipAddr = new IPAddressString(ip).toAddress();
        } catch (AddressStringException ex) {
            return false;
        }
        if (ipAddr == null) {
            return false;
        }
        return network.toPrefixBlock().contains(normalize(ipAddr));
    }

    private static IPAddress normalize(IPAddress addr) {
        if (addr.isIPv4Convertible()) {
            IPAddress converted = addr.toIPv4();
            return converted != null ? converted : addr;
        }
        return addr;
    }

    public String getNetworkRangeIp() {
        return new IPAddressString(value).getAddress().toPrefixBlock().toString();
    }
}
