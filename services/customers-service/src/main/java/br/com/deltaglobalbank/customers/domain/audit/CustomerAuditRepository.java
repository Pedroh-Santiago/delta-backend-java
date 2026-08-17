package br.com.deltaglobalbank.customers.domain.audit;

import java.util.List;

public interface CustomerAuditRepository {
    void saveAll(List<CustomerAuditEntry> entries);
}
