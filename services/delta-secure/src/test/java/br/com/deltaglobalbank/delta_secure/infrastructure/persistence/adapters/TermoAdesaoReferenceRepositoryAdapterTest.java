package br.com.deltaglobalbank.delta_secure.infrastructure.persistence.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import br.com.deltaglobalbank.delta_secure.TestcontainersConfiguration;
import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReference;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReferenceRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TermoAdesaoReferenceRepositoryAdapterTest {

    @Autowired
    private TermoAdesaoReferenceRepository repository;

    @Test
    void salvaEBuscaUmaReferenciaPeloTicket() {
        TermoAdesaoReference referencia = new TermoAdesaoReference(
            UUID.randomUUID(),
            "TCK-adapter-" + UUID.randomUUID(),
            148030,
            Convenio.CLT,
            "148030",
            Instant.now()
        );

        repository.save(referencia);
        TermoAdesaoReference encontrada = repository.findByTicket(referencia.ticket());

        assertEquals(referencia.ticket(), encontrada.ticket());
        assertEquals(referencia.heroSegurosId(), encontrada.heroSegurosId());
        assertEquals(referencia.convenio(), encontrada.convenio());
        assertEquals(referencia.externalId(), encontrada.externalId());
    }

    @Test
    void ticketDesconhecidoDevolveNulo() {
        assertNull(repository.findByTicket("ticket-que-nao-existe-" + UUID.randomUUID()));
    }
}
