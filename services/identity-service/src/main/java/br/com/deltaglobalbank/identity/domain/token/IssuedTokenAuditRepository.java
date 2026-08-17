package br.com.deltaglobalbank.identity.domain.token;

public interface IssuedTokenAuditRepository {
    void save(IssuedTokenAudit audit);

    IssuedTokenAudit findByJti(String jti);
}
