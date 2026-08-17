package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories;

import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.MakePixEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface JpaMakePixRepository extends JpaRepository<MakePixEntity, UUID> {
    Page<MakePixEntity> findByStatus(String status, Pageable pageable);
}
