package br.com.deltaglobalbank.internal_treasury.infrastructure.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaysmartTransferRequest(
    @JsonProperty("recipientAccountId")
    long accountNumber,

    @JsonProperty("transferAmount")
    int amount,

    @JsonProperty("freeDescription")
    String description
) {
}
