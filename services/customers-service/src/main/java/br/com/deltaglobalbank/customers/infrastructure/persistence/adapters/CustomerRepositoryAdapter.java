package br.com.deltaglobalbank.customers.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.BankAccountEntity;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.CustomerEntity;
import br.com.deltaglobalbank.customers.infrastructure.persistence.entities.PersonalDocumentEntity;
import br.com.deltaglobalbank.customers.infrastructure.persistence.mappers.BankAccountMapper;
import br.com.deltaglobalbank.customers.infrastructure.persistence.mappers.CustomerMapper;
import br.com.deltaglobalbank.customers.infrastructure.persistence.mappers.PersonalDocumentMapper;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaBankAccountRepository;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaCustomerRepository;
import br.com.deltaglobalbank.customers.infrastructure.persistence.repositories.JpaPersonalDocumentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final JpaBankAccountRepository jpaBankAccount;
    private final JpaPersonalDocumentRepository jpaDocument;
    private final JpaCustomerRepository jpaCustomer;

    public CustomerRepositoryAdapter(
        JpaBankAccountRepository jpaBankAccount,
        JpaPersonalDocumentRepository jpaDocument,
        JpaCustomerRepository jpaCustomer
    ) {
        this.jpaBankAccount = jpaBankAccount;
        this.jpaDocument = jpaDocument;
        this.jpaCustomer = jpaCustomer;
    }

    private Customer toDomainWithChildren(CustomerEntity entity) {
        var accounts = jpaBankAccount.findAllByCustomerId(entity.getId()).stream()
            .map(BankAccountMapper::toDomain)
            .toList();
        var documents = jpaDocument.findAllByCustomerId(entity.getId()).stream()
            .map(PersonalDocumentMapper::toDomain)
            .toList();
        return CustomerMapper.toDomain(entity, accounts, documents);
    }

    @Override
    @Transactional
    public Customer save(Customer customer) {
        CustomerEntity existing = jpaCustomer.findById(customer.id()).orElse(null);
        CustomerEntity entity = existing != null
            ? CustomerMapper.applyTo(customer, existing)
            : CustomerMapper.toEntity(customer);
        jpaCustomer.save(entity);

        syncBankAccounts(customer);
        syncDocuments(customer);

        return findById(customer.id(), customer.tenantId());
    }

    @Override
    public Customer findById(UUID id, UUID tenantId) {
        CustomerEntity entity = jpaCustomer.findByIdAndTenantId(id, tenantId);
        return entity != null ? toDomainWithChildren(entity) : null;
    }

    @Override
    public Customer findByCpfAndTenantId(Cpf cpf, UUID tenantId) {
        CustomerEntity entity = jpaCustomer.findByCpfAndTenantId(cpf.value(), tenantId);
        return entity != null ? toDomainWithChildren(entity) : null;
    }

    @Override
    public boolean existsByCpfAndTenantId(Cpf cpf, UUID tenantId) {
        return jpaCustomer.existsByCpfAndTenantId(cpf.value(), tenantId);
    }

    @Override
    public Page<Customer> findPage(UUID tenantId, Pageable pageable, String status, String cpf, String fullName) {
        return jpaCustomer.findPageFiltered(tenantId, status, cpf, fullName, pageable).map(this::toDomainWithChildren);
    }

    @Override
    public List<Customer> findAllByIds(List<UUID> ids, UUID tenantId) {
        return assemble(jpaCustomer.findAllByIdInAndTenantId(ids, tenantId));
    }

    @Override
    public List<Customer> searchByName(String query, UUID tenantId, int limit) {
        return assemble(jpaCustomer.searchByName(query, tenantId, PageRequest.of(0, limit)));
    }

    @Override
    @Transactional
    public void delete(Customer customer) {
        jpaBankAccount.deleteAll(jpaBankAccount.findAllByCustomerId(customer.id()));
        jpaDocument.deleteAll(jpaDocument.findAllByCustomerId(customer.id()));
        jpaCustomer.deleteById(customer.id());
    }

    private void syncBankAccounts(Customer customer) {
        List<BankAccountEntity> existing = jpaBankAccount.findAllByCustomerId(customer.id());
        Set<UUID> currentIds = customer.bankAccounts().stream()
            .map(account -> account.id())
            .collect(Collectors.toSet());

        jpaBankAccount.saveAll(customer.bankAccounts().stream().map(BankAccountMapper::toEntity).toList());
        jpaBankAccount.deleteAll(existing.stream().filter(it -> !currentIds.contains(it.getId())).toList());
    }

    private void syncDocuments(Customer customer) {
        List<PersonalDocumentEntity> existing = jpaDocument.findAllByCustomerId(customer.id());
        Set<UUID> currentIds = customer.documents().stream()
            .map(document -> document.id())
            .collect(Collectors.toSet());

        jpaDocument.saveAll(customer.documents().stream().map(PersonalDocumentMapper::toEntity).toList());
        jpaDocument.deleteAll(existing.stream().filter(it -> !currentIds.contains(it.getId())).toList());
    }

    private List<Customer> assemble(List<CustomerEntity> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        Set<UUID> ids = entities.stream().map(CustomerEntity::getId).collect(Collectors.toSet());
        Map<UUID, List<BankAccountEntity>> accts = jpaBankAccount.findAllByCustomerIdIn(ids).stream()
            .collect(Collectors.groupingBy(BankAccountEntity::getCustomerId));
        Map<UUID, List<PersonalDocumentEntity>> docs = jpaDocument.findAllByCustomerIdIn(ids).stream()
            .collect(Collectors.groupingBy(PersonalDocumentEntity::getCustomerId));
        return entities.stream()
            .map(entity -> CustomerMapper.toDomain(
                entity,
                accts.getOrDefault(entity.getId(), List.of()).stream().map(BankAccountMapper::toDomain).toList(),
                docs.getOrDefault(entity.getId(), List.of()).stream().map(PersonalDocumentMapper::toDomain).toList()
            ))
            .toList();
    }
}
