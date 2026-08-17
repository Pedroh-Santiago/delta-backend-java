package br.com.deltaglobalbank.identity.infrastructure.persistence.repositories;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaSigningKeyRepository extends JpaRepository<SigningKeyEntity, UUID> {
    SigningKeyEntity findByKid(String kid);

    List<SigningKeyEntity> findAllByStatus(String status);

    SigningKeyEntity findFirstByStatusOrderByActivatedAtDesc(String status);
}
