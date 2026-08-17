package br.com.deltaglobalbank.internal_treasury.features.approveInternalTransferece;

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository;
import br.com.deltaglobalbank.internal_treasury.infrastructure.messaging.TefPublisher;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ApproveInternalTransferenceUseCase {

    private final InternalTransferenceRepository repository;
    private final TefPublisher tefPublisher;

    public ApproveInternalTransferenceUseCase(
        InternalTransferenceRepository repository,
        TefPublisher tefPublisher
    ) {
        this.repository = repository;
        this.tefPublisher = tefPublisher;
    }

    public List<UUID> execute(ApproveInternalTransferenceRequest request) {
        List<UUID> approved = new ArrayList<>();

        for (UUID id : request.approved()) {
            InternalTransference transference = repository.findById(id);
            if (transference == null) {
                continue;
            }

            transference.approve();
            repository.save(transference);

            tefPublisher.publishApproval(id);

            approved.add(id);
        }
        return approved;
    }
}
