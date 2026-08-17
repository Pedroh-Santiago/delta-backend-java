package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters

import br.com.deltaglobalbank.internal_treasury.domain.account.BalanceService
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses.PaysmartBalanceResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PaysmartBalanceAdapter(
    private val restClient: RestClient,
    @Value("\${API_BASE_URL}") private val baseUrl: String,
    @Value("\${APIKEY_PAYSMART}") private val apiKey: String
) : BalanceService {

    override fun getBalance(accountId: Long): Long {
        val response = restClient.get()
            .uri("${baseUrl}/accounts/${accountId}/balance")
            .header("Content-Type", "application/json")
            .header("X-API-KEY", apiKey)
            .retrieve()
            .body(PaysmartBalanceResponse::class.java)

        return response?.balance ?: 0
    }
}