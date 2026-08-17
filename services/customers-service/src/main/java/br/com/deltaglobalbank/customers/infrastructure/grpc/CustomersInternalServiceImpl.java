package br.com.deltaglobalbank.customers.infrastructure.grpc;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccount;
import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose;
import br.com.deltaglobalbank.customers.domain.customer.Customer;
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository;
import br.com.deltaglobalbank.customers.domain.customer.CustomerSnapshot;
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf;
import br.com.deltaglobalbank.customers.grpc.BatchGetCustomersRequest;
import br.com.deltaglobalbank.customers.grpc.BatchGetCustomersResponse;
import br.com.deltaglobalbank.customers.grpc.CustomerSummary;
import br.com.deltaglobalbank.customers.grpc.CustomersInternalServiceGrpc;
import br.com.deltaglobalbank.customers.grpc.GetCustomerByCpfRequest;
import br.com.deltaglobalbank.customers.grpc.GetCustomerRequest;
import br.com.deltaglobalbank.customers.grpc.GetCustomerResponse;
import br.com.deltaglobalbank.customers.grpc.PrimaryDisbursementAccount;
import br.com.deltaglobalbank.customers.grpc.SearchCustomersRequest;
import br.com.deltaglobalbank.customers.grpc.SearchCustomersResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomersInternalServiceImpl extends CustomersInternalServiceGrpc.CustomersInternalServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(CustomersInternalServiceImpl.class);

    private final CustomerRepository customerRepository;

    public CustomersInternalServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    private CustomerSummary toSummary(Customer customer) {
        CustomerSnapshot s = customer.snapshot();
        CustomerSummary.Builder builder = CustomerSummary.newBuilder()
            .setId(s.id().toString())
            .setTenantId(s.tenantId().toString())
            .setCpf(s.cpf().value())
            .setFullName(s.fullName().value())
            .setBirthDate(s.birthDate().value().toString())
            .setStatus(s.status().toDatabaseValue())
            .setPhoneNumber(s.phoneNumber().value())
            .setEmail(s.email() != null ? s.email().value() : "");

        s.bankAccounts().stream()
            .filter(it -> it.isPrimary() && it.purpose() == BankAccountPurpose.DISBURSEMENT)
            .findFirst()
            .ifPresent(acc -> builder.setPrimaryDisbursementAccount(
                PrimaryDisbursementAccount.newBuilder()
                    .setBankCode(acc.bankCode().value())
                    .setAgency(acc.agency().value())
                    .setAccountNumber(acc.accountNumber())
                    .setAccountDigit(acc.accountDigit() != null ? acc.accountDigit() : "")
                    .setAccountType(acc.accountType().toDatabaseValue())
                    .build()
            ));
        return builder.build();
    }

    private void sendError(StreamObserver<?> observer, Status status, String code) {
        observer.onError(status.withDescription(code).asRuntimeException());
    }

    @Override
    public void getCustomer(GetCustomerRequest request, StreamObserver<GetCustomerResponse> responseObserver) {
        try {
            if (request.getTenantId().isBlank()) {
                sendError(responseObserver, Status.INVALID_ARGUMENT, "tenant_id_required");
                return;
            }
            UUID id = UUID.fromString(request.getCustomerId());
            UUID tenantId = UUID.fromString(request.getTenantId());
            Customer customer = customerRepository.findById(id, tenantId);
            if (customer == null) {
                sendError(responseObserver, Status.NOT_FOUND, "customer_not_found");
                return;
            }
            responseObserver.onNext(GetCustomerResponse.newBuilder().setCustomer(toSummary(customer)).build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            sendError(responseObserver, Status.INVALID_ARGUMENT, "invalid_id");
        } catch (Exception ex) {
            log.error("getCustomer erro inesperado", ex);
            sendError(responseObserver, Status.INTERNAL, "internal_error");
        }
    }

    @Override
    public void getCustomerByCpf(GetCustomerByCpfRequest request, StreamObserver<GetCustomerResponse> responseObserver) {
        try {
            if (request.getTenantId().isBlank()) {
                sendError(responseObserver, Status.INVALID_ARGUMENT, "tenant_id_required");
                return;
            }
            UUID tenantId = UUID.fromString(request.getTenantId());
            Cpf cpf = new Cpf(request.getCpf());
            Customer customer = customerRepository.findByCpfAndTenantId(cpf, tenantId);
            if (customer == null) {
                sendError(responseObserver, Status.NOT_FOUND, "customer_not_found");
                return;
            }
            responseObserver.onNext(GetCustomerResponse.newBuilder().setCustomer(toSummary(customer)).build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            sendError(responseObserver, Status.INVALID_ARGUMENT, ex.getMessage() != null ? ex.getMessage() : "invalid_argument");
        } catch (Exception ex) {
            log.error("getCustomerByCpf erro inesperado", ex);
            sendError(responseObserver, Status.INTERNAL, "internal_error");
        }
    }

    @Override
    public void batchGetCustomers(BatchGetCustomersRequest request, StreamObserver<BatchGetCustomersResponse> responseObserver) {
        try {
            if (request.getTenantId().isBlank()) {
                sendError(responseObserver, Status.INVALID_ARGUMENT, "tenant_id_required");
                return;
            }
            UUID tenantId = UUID.fromString(request.getTenantId());
            List<UUID> ids = request.getCustomerIdsList().stream().map(UUID::fromString).toList();
            List<Customer> customers = customerRepository.findAllByIds(ids, tenantId);
            responseObserver.onNext(
                BatchGetCustomersResponse.newBuilder()
                    .addAllCustomers(customers.stream().map(this::toSummary).toList())
                    .build()
            );
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            sendError(responseObserver, Status.INVALID_ARGUMENT, "invalid_id");
        } catch (Exception ex) {
            log.error("batchGetCustomers erro inesperado", ex);
            sendError(responseObserver, Status.INTERNAL, "internal_error");
        }
    }

    @Override
    public void searchCustomers(SearchCustomersRequest request, StreamObserver<SearchCustomersResponse> responseObserver) {
        try {
            if (request.getTenantId().isBlank()) {
                sendError(responseObserver, Status.INVALID_ARGUMENT, "tenant_id_required");
                return;
            }
            UUID tenantId = UUID.fromString(request.getTenantId());
            int limit = request.getLimit() <= 0 ? 10 : Math.min(request.getLimit(), 50);
            List<Customer> customers = customerRepository.searchByName(request.getQuery(), tenantId, limit);
            responseObserver.onNext(
                SearchCustomersResponse.newBuilder()
                    .addAllCustomers(customers.stream().map(this::toSummary).toList())
                    .build()
            );
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            sendError(responseObserver, Status.INVALID_ARGUMENT, "invalid_argument");
        } catch (Exception ex) {
            log.error("searchCustomers erro inesperado", ex);
            sendError(responseObserver, Status.INTERNAL, "internal_error");
        }
    }
}
