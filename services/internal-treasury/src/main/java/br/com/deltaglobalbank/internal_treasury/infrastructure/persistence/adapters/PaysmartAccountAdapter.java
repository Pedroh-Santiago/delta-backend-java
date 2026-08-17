package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters;

import br.com.deltaglobalbank.internal_treasury.domain.account.Account;
import br.com.deltaglobalbank.internal_treasury.domain.account.AccountRepository;
import br.com.deltaglobalbank.internal_treasury.domain.account.Cpf;
import br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses.PaysmartAccountResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;

@Component
public class PaysmartAccountAdapter implements AccountRepository {

    private final RestClient restClient;
    private final String baseUrl;

    public PaysmartAccountAdapter(
        RestClient restClient,
        @Value("${API_BASE_URL}") String baseUrl
    ) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public Account findByCpf(Cpf cpf) {
        List<PaysmartAccountResponse> response = restClient.get()
            .uri(baseUrl + "/accounts?document=" + cpf.value() + "&orderType=ASC&page=1&pageSize=10")
            .retrieve()
            .body(new ParameterizedTypeReference<List<PaysmartAccountResponse>>() {});

        if (response == null || response.isEmpty()) {
            return null;
        }
        PaysmartAccountResponse firstAccount = response.get(0);

        return new Account(
            firstAccount.accountId(),
            firstAccount.accountNumber(),
            new Cpf(firstAccount.personalDocument())
        );
    }
}
