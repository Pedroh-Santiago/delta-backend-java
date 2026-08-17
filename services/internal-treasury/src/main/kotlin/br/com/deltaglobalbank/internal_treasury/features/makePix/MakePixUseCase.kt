package br.com.deltaglobalbank.internal_treasury.features.makePix

import br.com.deltaglobalbank.internal_treasury.domain.account.BalanceService
import br.com.deltaglobalbank.internal_treasury.domain.makePix.InsufficientBalanceException
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository
import com.github.f4b6a3.uuid.UuidCreator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MakePixUseCase (
    private val makePixRepository: MakePixRepository,
    private val balanceService: BalanceService
){
    @Transactional
    fun execute(request: MakePixRequest) : MakePixResponse {
        val balance = balanceService.getBalance(request.accountId)
        if (balance < request.operationAmount) {
            throw InsufficientBalanceException()
        }

        val id = UuidCreator.getTimeOrderedEpoch()
        val now = Instant.now()

        val makePix = MakePix(
            id = id,
            accountId = request.accountId,
            recipientInstitutionCode = request.recipientInstitutionCode,
            recipientBranchCode = request.recipientBranchCode,
            recipientAccountNumber = request.recipientAccountNumber,
            recipientAccountType = request.recipientAccountType,
            recipientName = request.recipientName,
            operationAmount = request.operationAmount,
            createdAt = now,
        )

        makePixRepository.save(makePix)

        return MakePixResponse(
            id = id,
            accountId = request.accountId,
            operationAmount = request.operationAmount,
            createdAt = now
        )
    }
}