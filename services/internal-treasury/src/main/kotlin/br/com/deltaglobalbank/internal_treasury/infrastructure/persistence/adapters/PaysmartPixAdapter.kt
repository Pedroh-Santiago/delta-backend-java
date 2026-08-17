package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters

import br.com.deltaglobalbank.internal_treasury.domain.makePix.PaysmartPixException
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PixGateway
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.request.PaysmartPixRequest
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses.PaysmartPixResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.math.BigDecimal

@Component
class PaysmartPixAdapter (
    private val restClient: RestClient,
    @Value("\${API_BASE_URL}") private val baseUrl: String,
    @Value("\${APIKEY_PAYSMART}") private val apiKey: String
) : PixGateway {

    override fun transferPix(
        accountId: Long,
        recipientInstitutionCode: String,
        recipientBranchCode: String,
        recipientAccountNumber: String,
        recipientAccountType: String,
        recipientName: String,
        operationAmount: Long
    ): Long {
        val requestBody = PaysmartPixRequest(
            accountId = accountId,
            recipientInstitutionCode = recipientInstitutionCode,
            recipientBranchCode = recipientBranchCode,
            recipientAccountNumber = recipientAccountNumber,
            recipientAccountType = recipientAccountType,
            recipientName = recipientName,
            operationAmount = BigDecimal.valueOf(operationAmount, 2)
        )

        val response = restClient.post()
            .uri("$baseUrl/v1/pix/transactions/payment")
            .header("Content-Type", "application/json")
            .body(requestBody)
            .header("X-API-KEY", apiKey)
            .retrieve()
            .body (PaysmartPixResponse::class.java)


        if (response == null || !response.arbiPixTransferError?.erros.isNullOrEmpty()) {
            throw PaysmartPixException(accountId)
        }

        return response!!.idTransaction

    }
}