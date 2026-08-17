package br.com.deltaglobalbank.customers.domain.customer;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerRepository {
    Customer save(Customer customer);
    Customer findById(UUID id, UUID tenantId);
    Customer findByCpfAndTenantId(Cpf cpf, UUID tenantId);
    boolean existsByCpfAndTenantId(Cpf cpf, UUID tenantId);
    Page<Customer> findPage(UUID tenantId, Pageable pageable);
    List<Customer> findAllByIds(List<UUID> ids, UUID tenantId);
    List<Customer> searchByName(String query, UUID tenantId, int limit);
    void delete(Customer customer);
}
