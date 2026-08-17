package br.com.deltaglobalbank.customers.domain.audit

interface CustomerAuditRepository {
    fun saveAll(entries: List<CustomerAuditEntry>)
}