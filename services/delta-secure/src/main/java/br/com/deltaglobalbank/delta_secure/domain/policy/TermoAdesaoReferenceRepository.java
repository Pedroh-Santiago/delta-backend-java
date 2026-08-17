package br.com.deltaglobalbank.delta_secure.domain.policy;

public interface TermoAdesaoReferenceRepository {
    TermoAdesaoReference save(TermoAdesaoReference reference);

    TermoAdesaoReference findByTicket(String ticket);
}
