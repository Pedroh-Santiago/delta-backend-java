package br.com.deltaglobalbank.customers.domain.customer.valueobjects

import br.com.deltaglobalbank.customers.domain.shared.valueobjects.Uf

data class Address(
    val cep: String,
    val street: String,
    val city: String,
    val state: Uf,
    val country: String = "BR",
    val number: String? = null,
    val complement: String? = null,
    val neighborhood: String? = null,
) {
    init {
        require(CEP.matches(cep)) { "address_cep_invalid" }
        require(street.isNotBlank()) { "address_street_blank" }
        require(city.isNotBlank()) { "address_city_blank" }
    }
    companion object {
        private val CEP = Regex("^\\d{8}$")
    }
}