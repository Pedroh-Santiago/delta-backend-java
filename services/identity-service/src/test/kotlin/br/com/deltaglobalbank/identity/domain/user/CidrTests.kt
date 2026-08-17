package br.com.deltaglobalbank.identity.domain.user

import br.com.deltaglobalbank.identity.domain.ipAllowlist.Cidr
import br.com.deltaglobalbank.identity.domain.ipAllowlist.InvalidCidrException
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class CidrTests {
    @Test
    fun `must reject blank cidr`() {
        assertAll(
            { assertThrows(InvalidCidrException::class.java) { Cidr("") } },
            { assertThrows(InvalidCidrException::class.java) { Cidr("   ") } }
        )
    }

    @Nested
    inner class Validation {
        @ParameterizedTest(name = "valid: {0}")
        @ValueSource(strings = [
            "192.168.0.0/24",
            "10.0.0.0/8",
            "0.0.0.0/0",
            "192.168.0.1/32",
            "2001:db8::/32",
            "2001:db8::1/128"
        ])
        fun `must accept valid cidr`(validCidr: String) {
            assertDoesNotThrow { Cidr(validCidr) }
        }

        @ParameterizedTest(name = "invalid: {0}")
        @ValueSource(strings = [
            "192.168.0.1",
            "not.an.ip/24",
            "256.1.1.1/24",
            "192.168.0.0/33",
            "2001:db8::/129"
        ])
        fun `must reject invalid cidr`(invalidCidr: String) {
            assertThrows(InvalidCidrException::class.java) { Cidr(invalidCidr) }
        }
    }

    @Nested
    inner class Matches {

        @ParameterizedTest(name = "{0} contains {1} → {2}")
        @CsvSource(
            "192.168.0.0/24, 192.168.0.0,   true",
            "192.168.0.0/24, 192.168.0.1,   true",
            "192.168.0.0/24, 192.168.0.255, true",
            "192.168.0.0/24, 192.167.255.255, false",
            "192.168.0.0/24, 192.168.1.0,   false"
        )
        fun `must respect network boundaries`(cidr: String, ip: String, expected: Boolean) {
            assertEquals(expected, Cidr(cidr).matches(ip))
        }

        @ParameterizedTest(name = "{0} contains {1} → {2}")
        @CsvSource(
            "10.0.0.0/8,        10.255.255.255, true",
            "10.0.0.0/8,        11.0.0.0,       false",
            "192.168.0.1/32,    192.168.0.1,    true",
            "192.168.0.1/32,    192.168.0.2,    false",
            "0.0.0.0/0,         8.8.8.8,        true",
            "0.0.0.0/0,         255.255.255.255, true"
        )
        fun `must handle network sizes from single host to all`(cidr: String, ip: String, expected: Boolean) {
            assertEquals(expected, Cidr(cidr).matches(ip))
        }

        @ParameterizedTest(name = "{0} contains {1} → {2}")
        @CsvSource(
            "2001:db8::/32, 2001:db8::1,        true",
            "2001:db8::/32, 2001:db8:0:1::,     true",
            "2001:db8::/32, 2001:db9::1,        false"
        )
        fun `must match ipv6 addresses`(cidr: String, ip: String, expected: Boolean) {
            assertEquals(expected, Cidr(cidr).matches(ip))
        }

        @ParameterizedTest(name = "{0} contains {1} → {2}")
        @CsvSource(
            "192.168.0.0/24, ::ffff:192.168.0.5, true",
            "192.168.0.0/24, ::ffff:192.168.1.5, false"
        )
        fun `must normalize ipv4-mapped ipv6 addresses`(cidr: String, ip: String, expected: Boolean) {
            assertEquals(expected, Cidr(cidr).matches(ip))
        }

        @ParameterizedTest(name = "invalid input: {1} → false")
        @CsvSource(
            "192.168.0.0/24, not-an-ip,    false",
            "192.168.0.0/24, 999.999.999.999, false"
        )
        fun `must return false for invalid ip input`(cidr: String, ip: String, expected: Boolean) {
            assertEquals(expected, Cidr(cidr).matches(ip))
        }
    }

    @Nested
    inner class NetworkRange {

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource(
            "192.168.0.0/24,  192.168.0.0/24",
            "192.168.0.5/24,  192.168.0.0/24",
            "192.168.0.254/24, 192.168.0.0/24",
            "10.1.2.3/8,      10.0.0.0/8",
            "172.16.5.10/12,  172.16.0.0/12"
        )
        fun `must canonicalize ipv4 to network range`(input: String, expected: String) {
            assertEquals(expected, Cidr(input).getNetworkRangeIp())
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource(
            "2001:db8::/32,    2001:db8::/32",
            "2001:db8::5/32,   2001:db8::/32"
        )
        fun `must canonicalize ipv6 to network range`(input: String, expected: String) {
            assertEquals(expected, Cidr(input).getNetworkRangeIp())
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource(
            "0.0.0.0/0,        0.0.0.0/0",
            "192.168.0.1/32,   192.168.0.1/32"
        )
        fun `must handle extreme prefixes`(input: String, expected: String) {
            assertEquals(expected, Cidr(input).getNetworkRangeIp())
        }
    }
}