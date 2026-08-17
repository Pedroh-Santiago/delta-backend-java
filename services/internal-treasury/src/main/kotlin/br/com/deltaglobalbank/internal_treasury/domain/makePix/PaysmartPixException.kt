package br.com.deltaglobalbank.internal_treasury.domain.makePix

class PaysmartPixException (accountId: Long) : RuntimeException("Falha ao realizar o pix da conta $accountId")