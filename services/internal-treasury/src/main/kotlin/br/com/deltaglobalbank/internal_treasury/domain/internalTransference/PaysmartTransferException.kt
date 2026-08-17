package br.com.deltaglobalbank.internal_treasury.domain.internalTransference

class PaysmartTransferException (payerAccountNumber: Long) : RuntimeException("Falha ao processar transferência da conta $payerAccountNumber")