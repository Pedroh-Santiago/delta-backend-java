package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories;

import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.InternalTransferenceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaInternalTransferenceRepository extends JpaRepository<InternalTransferenceEntity, UUID> {
    Page<InternalTransferenceEntity> findByStatus(String status, Pageable pageable);
}
