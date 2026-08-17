package br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference

import br.com.deltaglobalbank.internal_treasury.domain.account.BalanceService
import br.com.deltaglobalbank.internal_treasury.domain.account.Cpf
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InsufficientBalanceException
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository
import br.com.deltaglobalbank.internal_treasury.features.retrieveAccount.RetrieveAccountRequest
import br.com.deltaglobalbank.internal_treasury.features.retrieveAccount.RetrieveAccountUseCase
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class OrderInternalTransferenceUseCase (
    private val internalTransferenceRepository: InternalTransferenceRepository,
    private val retrieveAccountUseCase: RetrieveAccountUseCase,
    private val balanceService: BalanceService
) {
    @Transactional
    fun execute(request: OrderInternalTransferenceRequest, requestedById: UUID): OrderInternalTransferenceResponse {
        val payerAccount = retrieveAccountUseCase.execute(
            RetrieveAccountRequest(cpf = Cpf(request.payerCpf))
        )

        val receiverAccount = retrieveAccountUseCase.execute(
            RetrieveAccountRequest(cpf = Cpf(request.receiverCpf))
        )

        val payerId = payerAccount.accountId
        val accountNumber = receiverAccount.accountNumber
        val id = UuidCreator.getTimeOrderedEpoch()
        val requestedAt = Instant.now()
        val balance = balanceService.getBalance(payerId)

        if (balance < request.amount) {
            throw InsufficientBalanceException()
        }

        val transference = InternalTransference(
            id = id,
            payerId = payerId,
            accountNumber = accountNumber,
            amount = request.amount,
            description = request.description,
            requestedAt = requestedAt,
            requestedById = requestedById
        )

        internalTransferenceRepository.save(transference)

        return OrderInternalTransferenceResponse(
            id = id,
            payerId = payerId,
            accountNumber = accountNumber,
            amount = request.amount,
            requestedAt = requestedAt
        )
    }

}