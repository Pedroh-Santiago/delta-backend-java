package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters;

import br.com.deltaglobalbank.internal_treasury.domain.makePix.PaysmartPixException;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PixGateway;
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.request.PaysmartPixRequest;
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses.PaysmartPixResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;

@Component
public class PaysmartPixAdapter implements PixGateway {

    private final RestClient restClient;
    private final String baseUrl;
    private final String apiKey;

    public PaysmartPixAdapter(
        RestClient restClient,
        @Value("${API_BASE_URL}") String baseUrl,
        @Value("${APIKEY_PAYSMART}") String apiKey
    ) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Override
    public long transferPix(
        long accountId,
        String recipientInstitutionCode,
        String recipientBranchCode,
        String recipientAccountNumber,
        String recipientAccountType,
        String recipientName,
        long operationAmount
    ) {
        PaysmartPixRequest requestBody = new PaysmartPixRequest(
            accountId,
            recipientInstitutionCode,
            recipientBranchCode,
            recipientAccountNumber,
            recipientAccountType,
            recipientName,
            BigDecimal.valueOf(operationAmount, 2)
        );

        PaysmartPixResponse response = restClient.post()
            .uri(baseUrl + "/v1/pix/transactions/payment")
            .header("Content-Type", "application/json")
            .body(requestBody)
            .header("X-API-KEY", apiKey)
            .retrieve()
            .body(PaysmartPixResponse.class);

        boolean hasErros = response != null
            && response.arbiPixTransferError() != null
            && response.arbiPixTransferError().erros() != null
            && !response.arbiPixTransferError().erros().isEmpty();

        if (response == null || hasErros) {
            throw new PaysmartPixException(accountId);
        }

        return response.idTransaction();
    }
}
