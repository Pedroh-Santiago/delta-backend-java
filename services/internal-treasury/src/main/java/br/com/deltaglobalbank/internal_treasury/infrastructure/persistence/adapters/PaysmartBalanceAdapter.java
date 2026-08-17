package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters;

import br.com.deltaglobalbank.internal_treasury.domain.account.BalanceService;
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses.PaysmartBalanceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaysmartBalanceAdapter implements BalanceService {

    private final RestClient restClient;
    private final String baseUrl;
    private final String apiKey;

    public PaysmartBalanceAdapter(
        RestClient restClient,
        @Value("${API_BASE_URL}") String baseUrl,
        @Value("${APIKEY_PAYSMART}") String apiKey
    ) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Override
    public long getBalance(long accountId) {
        PaysmartBalanceResponse response = restClient.get()
            .uri(baseUrl + "/accounts/" + accountId + "/balance")
            .header("Content-Type", "application/json")
            .header("X-API-KEY", apiKey)
            .retrieve()
            .body(PaysmartBalanceResponse.class);

        return response == null ? 0 : response.balance();
    }
}
