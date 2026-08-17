package br.com.deltaglobalbank.internal_treasury.domain.makePix;

import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface MakePixRepository {
    MakePix save(MakePix makePix);
    Page<MakePix> findByStatus(PaymentsStatus status, Pageable pageable);
    Page<MakePix> findAll(Pageable pageable);
    MakePix findById(UUID id);
}
