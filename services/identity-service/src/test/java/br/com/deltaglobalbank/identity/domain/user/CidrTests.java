package br.com.deltaglobalbank.identity.domain.user;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr;
import br.com.deltaglobalbank.identity.domain.ipAllowlist.InvalidCidrException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CidrTests {

    @Test
    void mustRejectBlankCidr() {
        assertAll(
            () -> assertThrows(InvalidCidrException.class, () -> new Cidr("")),
            () -> assertThrows(InvalidCidrException.class, () -> new Cidr("   "))
        );
    }

    @Nested
    class Validation {

        @ParameterizedTest(name = "valid: {0}")
        @ValueSource(strings = {
            "192.168.0.0/24",
            "10.0.0.0/8",
            "0.0.0.0/0",
            "192.168.0.1/32",
            "2001:db8::/32",
            "2001:db8::1/128"
        })
        void mustAcceptValidCidr(String validCidr) {
            assertDoesNotThrow(() -> new Cidr(validCidr));
        }

        @ParameterizedTest(name = "invalid: {0}")
        @ValueSource(strings = {
            "192.168.0.1",
            "not.an.ip/24",
            "256.1.1.1/24",
            "192.168.0.0/33",
            "2001:db8::/129"
        })
        void mustRejectInvalidCidr(String invalidCidr) {
            assertThrows(InvalidCidrException.class, () -> new Cidr(invalidCidr));
        }
    }

    @Nested
    class Matches {

        @ParameterizedTest(name = "{0} contains {1} -> {2}")
        @CsvSource({
            "192.168.0.0/24, 192.168.0.0,   true",
            "192.168.0.0/24, 192.168.0.1,   true",
            "192.168.0.0/24, 192.168.0.255, true",
            "192.168.0.0/24, 192.167.255.255, false",
            "192.168.0.0/24, 192.168.1.0,   false"
        })
        void mustRespectNetworkBoundaries(String cidr, String ip, boolean expected) {
            assertEquals(expected, new Cidr(cidr).matches(ip));
        }

        @ParameterizedTest(name = "{0} contains {1} -> {2}")
        @CsvSource({
            "10.0.0.0/8,        10.255.255.255, true",
            "10.0.0.0/8,        11.0.0.0,       false",
            "192.168.0.1/32,    192.168.0.1,    true",
            "192.168.0.1/32,    192.168.0.2,    false",
            "0.0.0.0/0,         8.8.8.8,        true",
            "0.0.0.0/0,         255.255.255.255, true"
        })
        void mustHandleNetworkSizesFromSingleHostToAll(String cidr, String ip, boolean expected) {
            assertEquals(expected, new Cidr(cidr).matches(ip));
        }

        @ParameterizedTest(name = "{0} contains {1} -> {2}")
        @CsvSource({
            "2001:db8::/32, 2001:db8::1,        true",
            "2001:db8::/32, 2001:db8:0:1::,     true",
            "2001:db8::/32, 2001:db9::1,        false"
        })
        void mustMatchIpv6Addresses(String cidr, String ip, boolean expected) {
            assertEquals(expected, new Cidr(cidr).matches(ip));
        }

        @ParameterizedTest(name = "{0} contains {1} -> {2}")
        @CsvSource({
            "192.168.0.0/24, ::ffff:192.168.0.5, true",
            "192.168.0.0/24, ::ffff:192.168.1.5, false"
        })
        void mustNormalizeIpv4MappedIpv6Addresses(String cidr, String ip, boolean expected) {
            assertEquals(expected, new Cidr(cidr).matches(ip));
        }

        @ParameterizedTest(name = "invalid input: {1} -> false")
        @CsvSource({
            "192.168.0.0/24, not-an-ip,    false",
            "192.168.0.0/24, 999.999.999.999, false"
        })
        void mustReturnFalseForInvalidIpInput(String cidr, String ip, boolean expected) {
            assertEquals(expected, new Cidr(cidr).matches(ip));
        }
    }

    @Nested
    class NetworkRange {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
            "192.168.0.0/24,  192.168.0.0/24",
            "192.168.0.5/24,  192.168.0.0/24",
            "192.168.0.254/24, 192.168.0.0/24",
            "10.1.2.3/8,      10.0.0.0/8",
            "172.16.5.10/12,  172.16.0.0/12"
        })
        void mustCanonicalizeIpv4ToNetworkRange(String input, String expected) {
            assertEquals(expected, new Cidr(input).getNetworkRangeIp());
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
            "2001:db8::/32,    2001:db8::/32",
            "2001:db8::5/32,   2001:db8::/32"
        })
        void mustCanonicalizeIpv6ToNetworkRange(String input, String expected) {
            assertEquals(expected, new Cidr(input).getNetworkRangeIp());
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
            "0.0.0.0/0,        0.0.0.0/0",
            "192.168.0.1/32,   192.168.0.1/32"
        })
        void mustHandleExtremePrefixes(String input, String expected) {
            assertEquals(expected, new Cidr(input).getNetworkRangeIp());
        }
    }
}
