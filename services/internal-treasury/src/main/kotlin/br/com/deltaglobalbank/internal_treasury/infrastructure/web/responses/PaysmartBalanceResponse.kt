package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaysmartBalanceResponse(
    val message: String,
    val idAccount: Long,
    val balance: Long,
    val dateTime: String,
    val blockedBalance: Long,
    val availableBlockedBalance: Long,
    val realBalance: Long
)