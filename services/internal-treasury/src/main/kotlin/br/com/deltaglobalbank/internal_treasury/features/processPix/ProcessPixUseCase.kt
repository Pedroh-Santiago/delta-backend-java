package br.com.deltaglobalbank.internal_treasury.features.processPix

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PixGateway
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PixNotFoundException
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

import java.util.UUID

@Service
class ProcessPixUseCase (
    private val makePixRepository: MakePixRepository,
    private val pixGateway: PixGateway
){
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun execute(id: UUID){
        val transferece = makePixRepository.findById(id) ?: run{
            log.error("PIX $id não foi encontrado na fila")
            throw PixNotFoundException(id)
        }

        if (transferece.status == PaymentsStatus.PAID){
            log.info("PIX $id já está pago")
            return
        }

        val transactionId = pixGateway.transferPix(
            accountId = transferece.accountId,
            recipientInstitutionCode = transferece.recipientInstitutionCode,
            recipientBranchCode = transferece.recipientBranchCode,
            recipientAccountNumber = transferece.recipientAccountNumber,
            recipientAccountType = transferece.recipientAccountType,
            recipientName = transferece.recipientName,
            operationAmount = transferece.operationAmount
        )

        log.info("PIX $id pago na Paysmart (trasactionId=$transactionId) persistindo status localmente")

        transferece.markAsPaid(Instant.now())

        makePixRepository.save(transferece)

        log.info("PIX $id persistindo como PAGO com sucesso")
    }
}