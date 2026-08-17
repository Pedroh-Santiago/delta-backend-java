package br.com.deltaglobalbank.delta_secure.infrastructure.persistence.repositories;

import br.com.deltaglobalbank.delta_secure.infrastructure.persistence.entities.TermoAdesaoReferenceEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTermoAdesaoReferenceRepository extends JpaRepository<TermoAdesaoReferenceEntity, UUID> {
    TermoAdesaoReferenceEntity findByTicket(String ticket);
}
