package br.com.deltaglobalbank.internal_treasury.features.retrieveAccount

import br.com.deltaglobalbank.internal_treasury.domain.account.AccountNotFoundException
import br.com.deltaglobalbank.internal_treasury.domain.account.AccountRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RetrieveAccountUseCase (
    private val accountRepository: AccountRepository
){
    @Transactional
    fun execute(request: RetrieveAccountRequest): RetrieveAccountResponse {
        val account = accountRepository.findByCpf(request.cpf)
            ?: throw AccountNotFoundException()

        return RetrieveAccountResponse(
            accountId = account.accountId,
            accountNumber = account.accountNumber
        )
    }
}