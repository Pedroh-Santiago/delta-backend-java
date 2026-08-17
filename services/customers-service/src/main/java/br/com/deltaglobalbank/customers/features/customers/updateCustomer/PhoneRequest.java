package br.com.deltaglobalbank.customers.features.customers.updateCustomer;

import jakarta.validation.constraints.NotBlank;

public record PhoneRequest(@NotBlank String phoneNumber) {
}
