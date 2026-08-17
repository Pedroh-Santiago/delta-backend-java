package br.com.deltaglobalbank.products.domain.product

@JvmInline
value class AgreementName(val value: String) {
    init { require(value.isNotBlank()) { "agreement_name_required" } }
}

@JvmInline
value class DisplayName(val value: String)   {
    init { require(value.isNotBlank()) { "display_name_required" } }
}