package br.com.deltaglobalbank.internal_treasury.features.approvePix;

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository;
import br.com.deltaglobalbank.internal_treasury.infrastructure.messaging.PixPublisher;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ApprovePixUseCase {

    private final MakePixRepository repository;
    private final PixPublisher pixPublisher;

    public ApprovePixUseCase(MakePixRepository repository, PixPublisher pixPublisher) {
        this.repository = repository;
        this.pixPublisher = pixPublisher;
    }

    public List<UUID> execute(ApprovePixRequest request) {
        List<UUID> approved = new ArrayList<>();

        for (UUID id : request.approved()) {
            MakePix transference = repository.findById(id);
            if (transference == null) {
                continue;
            }

            transference.approve();
            repository.save(transference);

            pixPublisher.publishApproval(id);

            approved.add(id);
        }
        return approved;
    }
}
