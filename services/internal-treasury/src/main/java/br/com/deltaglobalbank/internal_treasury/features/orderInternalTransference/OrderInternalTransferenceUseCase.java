package br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference;

import br.com.deltaglobalbank.internal_treasury.domain.account.BalanceService;
import br.com.deltaglobalbank.internal_treasury.domain.account.Cpf;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InsufficientBalanceException;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository;
import br.com.deltaglobalbank.internal_treasury.features.retrieveAccount.RetrieveAccountRequest;
import br.com.deltaglobalbank.internal_treasury.features.retrieveAccount.RetrieveAccountResponse;
import br.com.deltaglobalbank.internal_treasury.features.retrieveAccount.RetrieveAccountUseCase;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderInternalTransferenceUseCase {

    private final InternalTransferenceRepository internalTransferenceRepository;
    private final RetrieveAccountUseCase retrieveAccountUseCase;
    private final BalanceService balanceService;

    public OrderInternalTransferenceUseCase(
        InternalTransferenceRepository internalTransferenceRepository,
        RetrieveAccountUseCase retrieveAccountUseCase,
        BalanceService balanceService
    ) {
        this.internalTransferenceRepository = internalTransferenceRepository;
        this.retrieveAccountUseCase = retrieveAccountUseCase;
        this.balanceService = balanceService;
    }

    @Transactional
    public OrderInternalTransferenceResponse execute(OrderInternalTransferenceRequest request, UUID requestedById) {
        RetrieveAccountResponse payerAccount = retrieveAccountUseCase.execute(
            new RetrieveAccountRequest(new Cpf(request.payerCpf()))
        );

        RetrieveAccountResponse receiverAccount = retrieveAccountUseCase.execute(
            new RetrieveAccountRequest(new Cpf(request.receiverCpf()))
        );

        long payerId = payerAccount.accountId();
        long accountNumber = receiverAccount.accountNumber();
        UUID id = UuidCreator.getTimeOrderedEpoch();
        Instant requestedAt = Instant.now();
        long balance = balanceService.getBalance(payerId);

        if (balance < request.amount()) {
            throw new InsufficientBalanceException();
        }

        InternalTransference transference = new InternalTransference(
            id,
            accountNumber,
            payerId,
            request.amount(),
            request.description(),
            requestedAt,
            requestedById
        );

        internalTransferenceRepository.save(transference);

        return new OrderInternalTransferenceResponse(
            id,
            payerId,
            accountNumber,
            request.amount(),
            requestedAt
        );
    }
}
