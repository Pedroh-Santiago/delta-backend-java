package br.com.deltaglobalbank.internal_treasury.domain.makePix

import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import java.time.Instant
import java.util.UUID

class MakePix (
    val id: UUID,
    val accountId: Long,
    val recipientInstitutionCode: String,
    val recipientBranchCode: String,
    val recipientAccountNumber: String,
    status: PaymentsStatus = PaymentsStatus.WAITING,
    paidAt: Instant? = null,
    val recipientAccountType: String,
    val recipientName: String,
    val operationAmount: Long,
    val createdAt: Instant
) {
    var status: PaymentsStatus = status
        private set

    var paidAt: Instant? = null
        private set

    fun approve(){
        require(status == PaymentsStatus.WAITING) {"Apenas PIX com status de Waiting podem ser alterados"}
        status = PaymentsStatus.APPROVED
    }

    fun markAsPaid(paidAt: Instant){
        require(status == PaymentsStatus.APPROVED) {"Apenas PIX que foram aprovados podem ser marcados como pago"}
        status = PaymentsStatus.PAID
        this.paidAt = paidAt
    }

    init {
        require(accountId > 0) { "Account Id precisa ser positivo" }
        require(recipientInstitutionCode.isNotBlank()) { "Código da instituição é obrigatório" }
        require(recipientBranchCode.isNotBlank()) { "Agência da conta é obrigatória" }
        require(recipientAccountNumber.isNotBlank()) { "Número da conta do recebedor é obrigatória" }
        require(recipientAccountType.isNotBlank()) { "Tipo da conta do recebedor é obrigatório" }
        require(recipientName.isNotBlank()) { "Nome do recebedor é obrigatório" }
        require(operationAmount > 0) { "Valor da operação precisa ser positivo" }
    }
}