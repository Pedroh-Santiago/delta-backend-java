package br.com.deltaglobalbank.internal_treasury.features.processTef;

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.PaysmartGateway;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.TefNotFoundException;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class ProcessTefUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessTefUseCase.class);

    private final InternalTransferenceRepository internalTransferenceRepository;
    private final PaysmartGateway paysmartGateway;

    public ProcessTefUseCase(
        InternalTransferenceRepository internalTransferenceRepository,
        PaysmartGateway paysmartGateway
    ) {
        this.internalTransferenceRepository = internalTransferenceRepository;
        this.paysmartGateway = paysmartGateway;
    }

    @Transactional
    public void execute(UUID id) {
        InternalTransference tranference = internalTransferenceRepository.findById(id);
        if (tranference == null) {
            log.error("TEF " + id + " não foi encontrado na fila");
            throw new TefNotFoundException(id);
        }

        if (tranference.status() == PaymentsStatus.PAID) {
            log.info("TEF " + id + " já está pago");
            return;
        }

        long transactionId = paysmartGateway.transfer(
            tranference.payerId(),
            tranference.accountNumber(),
            tranference.amount(),
            tranference.description()
        );

        log.info("TEF " + id + " pago na Paysmart (transactionId=" + transactionId + ") persistindo status localmente");

        tranference.markAsPaid(Instant.now());

        internalTransferenceRepository.save(tranference);

        log.info("TEF " + id + " persistido como PAGO com sucesso");
    }
}
