package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.PaysmartGateway
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.PaysmartTransferException
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.request.PaysmartTransferRequest
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses.PaysmartTransferResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PaysmartTransferAdapter (
    private val restClient: RestClient,
    @Value("\${API_BASE_URL}") private val baseUrl: String,
    @Value("\${APIKEY_PAYSMART}") private val apiKey: String
) : PaysmartGateway {

    override fun transfer(
        payerAccountNumber: Long,
        recipientAccountNumber: Long,
        amount: Int,
        description: String
    ): Long {
        val requestBody = PaysmartTransferRequest(
            accountNumber = recipientAccountNumber,
            amount = amount,
            description = description
        )

        val response = restClient.post()
            .uri("$baseUrl/accounts/${payerAccountNumber}/transfer")
            .header("Content-Type", "application/json")
            .body(requestBody)
            .header("X-API-KEY", apiKey)
            .retrieve()
            .body (PaysmartTransferResponse::class.java)

        return response?.transactionId ?: run {
            throw PaysmartTransferException(payerAccountNumber)
        }
    }
}