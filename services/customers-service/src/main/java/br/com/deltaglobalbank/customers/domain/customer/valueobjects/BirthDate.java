package br.com.deltaglobalbank.customers.domain.customer.valueobjects;

import java.time.LocalDate;

public record BirthDate(LocalDate value) {
    public BirthDate {
        LocalDate minAgeDate = LocalDate.now().minusYears(18);
        if (value.isAfter(minAgeDate)) {
            throw new IllegalArgumentException("customer_underage");
        }
    }
}
