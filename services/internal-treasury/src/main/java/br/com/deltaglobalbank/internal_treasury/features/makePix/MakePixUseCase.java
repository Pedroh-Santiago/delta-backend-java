package br.com.deltaglobalbank.internal_treasury.features.makePix;

import br.com.deltaglobalbank.internal_treasury.domain.account.BalanceService;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.InsufficientBalanceException;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class MakePixUseCase {

    private final MakePixRepository makePixRepository;
    private final BalanceService balanceService;

    public MakePixUseCase(MakePixRepository makePixRepository, BalanceService balanceService) {
        this.makePixRepository = makePixRepository;
        this.balanceService = balanceService;
    }

    @Transactional
    public MakePixResponse execute(MakePixRequest request) {
        long balance = balanceService.getBalance(request.accountId());
        if (balance < request.operationAmount()) {
            throw new InsufficientBalanceException();
        }

        UUID id = UuidCreator.getTimeOrderedEpoch();
        Instant now = Instant.now();

        MakePix makePix = new MakePix(
            id,
            request.accountId(),
            request.recipientInstitutionCode(),
            request.recipientBranchCode(),
            request.recipientAccountNumber(),
            request.recipientAccountType(),
            request.recipientName(),
            request.operationAmount(),
            now
        );

        makePixRepository.save(makePix);

        return new MakePixResponse(
            id,
            request.accountId(),
            request.operationAmount(),
            now
        );
    }
}
