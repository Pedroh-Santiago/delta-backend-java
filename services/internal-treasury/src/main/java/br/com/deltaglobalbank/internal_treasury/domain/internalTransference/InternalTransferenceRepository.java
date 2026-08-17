package br.com.deltaglobalbank.internal_treasury.domain.internalTransference;

import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface InternalTransferenceRepository {
    InternalTransference save(InternalTransference internalTransference);
    Page<InternalTransference> findByStatus(PaymentsStatus status, Pageable pageable);
    Page<InternalTransference> findAll(Pageable pageable);
    InternalTransference findById(UUID id);
}
