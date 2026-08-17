package br.com.deltaglobalbank.customers.domain.customer.valueobjects

import java.time.LocalDate

@JvmInline
value class BirthDate(val value: LocalDate) {
    init {
        val minAgeDate = LocalDate.now().minusYears(18)
        require(!value.isAfter(minAgeDate)) { "customer_underage" }
    }
}