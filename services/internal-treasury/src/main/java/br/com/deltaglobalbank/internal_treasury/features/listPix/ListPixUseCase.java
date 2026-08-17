package br.com.deltaglobalbank.internal_treasury.features.listPix;

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListPixUseCase {

    private final MakePixRepository repository;

    public ListPixUseCase(MakePixRepository repository) {
        this.repository = repository;
    }

    public ListPixResponse execute(int page, int pageSize, String filter) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<MakePix> result;
        if (filter.equals("all")) {
            result = repository.findAll(pageable);
        } else {
            PaymentsStatus status = PaymentsStatus.valueOf(filter.toUpperCase());
            result = repository.findByStatus(status, pageable);
        }

        List<MakePixItem> content = result.getContent().stream()
            .map(it -> new MakePixItem(
                it.id(),
                it.accountId(),
                it.recipientName(),
                it.recipientAccountNumber(),
                it.operationAmount(),
                it.status().name(),
                it.createdAt()
            ))
            .toList();

        return new ListPixResponse(
            content,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }
}
