package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaysmartBalanceResponse(
    String message,
    long idAccount,
    long balance,
    String dateTime,
    long blockedBalance,
    long availableBlockedBalance,
    long realBalance
) {
}
