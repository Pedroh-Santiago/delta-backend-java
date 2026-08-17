package br.com.deltaglobalbank.customers.features.customers.createCustomer;

import jakarta.validation.constraints.NotBlank;

public record PhoneRequest(@NotBlank String phoneNumber) {
}
