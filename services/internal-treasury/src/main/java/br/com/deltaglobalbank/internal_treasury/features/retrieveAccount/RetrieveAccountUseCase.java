package br.com.deltaglobalbank.internal_treasury.features.retrieveAccount;

import br.com.deltaglobalbank.internal_treasury.domain.account.Account;
import br.com.deltaglobalbank.internal_treasury.domain.account.AccountNotFoundException;
import br.com.deltaglobalbank.internal_treasury.domain.account.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RetrieveAccountUseCase {

    private final AccountRepository accountRepository;

    public RetrieveAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public RetrieveAccountResponse execute(RetrieveAccountRequest request) {
        Account account = accountRepository.findByCpf(request.cpf());
        if (account == null) {
            throw new AccountNotFoundException();
        }

        return new RetrieveAccountResponse(
            account.accountId(),
            account.accountNumber()
        );
    }
}
