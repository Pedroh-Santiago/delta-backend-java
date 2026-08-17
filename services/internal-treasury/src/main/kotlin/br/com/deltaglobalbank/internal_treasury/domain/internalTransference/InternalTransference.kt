package br.com.deltaglobalbank.internal_treasury.domain.internalTransference

import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import java.time.Instant
import java.util.UUID

class  InternalTransference(
    val id: UUID,
    val accountNumber: Long,
    val payerId: Long,
    paidAt: Instant? = null,
    val amount: Int,
    status: PaymentsStatus = PaymentsStatus.WAITING,
    val description: String,
    val requestedAt: Instant,
    val requestedById: UUID
) {
    var status: PaymentsStatus = status
    private set

    var paidAt: Instant? = null
    private set

    fun approve(){
        require(status == PaymentsStatus.WAITING) {"Apenas TEF'S com status de Waiting podem ser alterados"}
        status = PaymentsStatus.APPROVED
    }

    fun markAsPaid(paidAt: Instant){
        require(status == PaymentsStatus.APPROVED) {"Podemos marcar como pago apenas os que já foram aprovados"}
        status = PaymentsStatus.PAID
        this.paidAt = paidAt
    }
}

