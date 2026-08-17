package br.com.deltaglobalbank.internal_treasury.features.processTef

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.PaysmartGateway
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.TefNotFoundException
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.jvm.javaClass

@Service
class ProcessTefUseCase (
    private val internalTransferenceRepository: InternalTransferenceRepository,
    private val paysmartGateway: PaysmartGateway
){
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun execute(id: UUID){
        val tranference = internalTransferenceRepository.findById(id) ?: run{
            log.error("TEF $id não foi encontrado na fila")
            throw TefNotFoundException(id)
        }

        if (tranference.status == PaymentsStatus.PAID){
            log.info("TEF $id já está pago")
            return
        }

        val transactionId = paysmartGateway.transfer(
            payerAccountNumber = tranference.payerId,
            recipientAccountNumber = tranference.accountNumber,
            amount = tranference.amount,
            description = tranference.description
        )

        log.info("TEF $id pago na Paysmart (transactionId=$transactionId) persistindo status localmente")

        tranference.markAsPaid(Instant.now())

        internalTransferenceRepository.save(tranference)

        log.info("TEF $id persistido como PAGO com sucesso")
    }
}