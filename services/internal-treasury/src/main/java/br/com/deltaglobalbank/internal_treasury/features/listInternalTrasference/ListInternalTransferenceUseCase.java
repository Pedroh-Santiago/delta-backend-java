package br.com.deltaglobalbank.internal_treasury.features.listInternalTrasference;

import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransference;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InternalTransferenceRepository;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListInternalTransferenceUseCase {

    private final InternalTransferenceRepository repository;

    public ListInternalTransferenceUseCase(InternalTransferenceRepository repository) {
        this.repository = repository;
    }

    public ListInternalTransferenceResponse execute(int page, int pageSize, String filter) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<InternalTransference> result;
        if (filter.equals("all")) {
            result = repository.findAll(pageable);
        } else {
            PaymentsStatus status = PaymentsStatus.valueOf(filter.toUpperCase());
            result = repository.findByStatus(status, pageable);
        }

        List<InternalTransferenceItem> content = result.getContent().stream()
            .map(it -> new InternalTransferenceItem(
                it.id(),
                it.payerId(),
                it.accountNumber(),
                it.amount(),
                it.status().name(),
                it.requestedAt()
            ))
            .toList();

        return new ListInternalTransferenceResponse(
            content,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }
}
