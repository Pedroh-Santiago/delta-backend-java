package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters;

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.PaysmartGateway;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.PaysmartTransferException;
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.request.PaysmartTransferRequest;
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses.PaysmartTransferResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaysmartTransferAdapter implements PaysmartGateway {

    private final RestClient restClient;
    private final String baseUrl;
    private final String apiKey;

    public PaysmartTransferAdapter(
        RestClient restClient,
        @Value("${API_BASE_URL}") String baseUrl,
        @Value("${APIKEY_PAYSMART}") String apiKey
    ) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Override
    public long transfer(
        long payerAccountNumber,
        long recipientAccountNumber,
        int amount,
        String description
    ) {
        PaysmartTransferRequest requestBody = new PaysmartTransferRequest(
            recipientAccountNumber,
            amount,
            description
        );

        PaysmartTransferResponse response = restClient.post()
            .uri(baseUrl + "/accounts/" + payerAccountNumber + "/transfer")
            .header("Content-Type", "application/json")
            .body(requestBody)
            .header("X-API-KEY", apiKey)
            .retrieve()
            .body(PaysmartTransferResponse.class);

        if (response == null) {
            throw new PaysmartTransferException(payerAccountNumber);
        }
        return response.transactionId();
    }
}
