package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters

import br.com.deltaglobalbank.internal_treasury.domain.account.Account
import br.com.deltaglobalbank.internal_treasury.domain.account.AccountRepository
import br.com.deltaglobalbank.internal_treasury.domain.account.Cpf
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses.PaysmartAccountResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PaysmartAccountAdapter (
    private val restClient: RestClient,
    @Value("\${API_BASE_URL}") private val baseUrl: String
) : AccountRepository {
    override fun findByCpf(cpf: Cpf): Account? {
        val response = restClient.get()
            .uri("${baseUrl}/accounts?document=${cpf.value}&orderType=ASC&page=1&pageSize=10")
            .retrieve()
            .body(object : ParameterizedTypeReference<List<PaysmartAccountResponse>>() {})

        val firstAccount = response?.firstOrNull() ?: return null

        return Account(
            accountId = firstAccount.accountId,
            accountNumber = firstAccount.accountNumber,
            personalDocument = Cpf(firstAccount.personalDocument)
        )
    }
}