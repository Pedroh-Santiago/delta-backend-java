package br.com.deltaglobalbank.identity.domain.ipAllowlist

import inet.ipaddr.AddressStringException
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString

@JvmInline
 value class Cidr (val value: String){
     init{
         val parsed = IPAddressString(value)
         if (value.isBlank() || !parsed.isValid() || !parsed.isPrefixed){
             throw InvalidCidrException()
         }
     }

    fun matches(ip: String): Boolean {
        val network = IPAddressString(value).toAddress() ?: return false
        val ipAddr = try {
            IPAddressString(ip).toAddress() ?: return false
        }catch (ex: AddressStringException){
            return false
        }
        return network.toPrefixBlock().contains(normalize(ipAddr))
    }

    private fun normalize(addr: IPAddress): IPAddress {
        return if (addr.isIPv4Convertible) (addr.toIPv4() ?: addr) else addr
    }

    fun getNetworkRangeIp() : String =
        IPAddressString(value).toAddress()!!.toPrefixBlock().toString()
 }