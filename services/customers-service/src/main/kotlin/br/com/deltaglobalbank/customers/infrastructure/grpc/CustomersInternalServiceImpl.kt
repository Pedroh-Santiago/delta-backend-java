package br.com.deltaglobalbank.customers.infrastructure.grpc

import br.com.deltaglobalbank.customers.domain.bankAccount.BankAccountPurpose
import br.com.deltaglobalbank.customers.domain.customer.Customer
import br.com.deltaglobalbank.customers.domain.customer.CustomerRepository
import br.com.deltaglobalbank.customers.domain.customer.valueobjects.Cpf
import br.com.deltaglobalbank.customers.grpc.BatchGetCustomersRequest
import br.com.deltaglobalbank.customers.grpc.BatchGetCustomersResponse
import br.com.deltaglobalbank.customers.grpc.CustomerSummary
import br.com.deltaglobalbank.customers.grpc.CustomersInternalServiceGrpc
import br.com.deltaglobalbank.customers.grpc.GetCustomerByCpfRequest
import br.com.deltaglobalbank.customers.grpc.GetCustomerRequest
import br.com.deltaglobalbank.customers.grpc.GetCustomerResponse
import br.com.deltaglobalbank.customers.grpc.PrimaryDisbursementAccount
import br.com.deltaglobalbank.customers.grpc.SearchCustomersRequest
import br.com.deltaglobalbank.customers.grpc.SearchCustomersResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CustomersInternalServiceImpl(
    private val customerRepository: CustomerRepository
): CustomersInternalServiceGrpc.CustomersInternalServiceImplBase() {
    private fun toSummary(customer: Customer): CustomerSummary {
        val s = customer.snapshot()
        val builder = CustomerSummary.newBuilder()
            .setId(s.id.toString())
            .setTenantId(s.tenantId.toString())
            .setCpf(s.cpf.value)
            .setFullName(s.fullName.value)
            .setBirthDate(s.birthDate.value.toString())
            .setStatus(s.status.toDatabaseValue())
            .setPhoneNumber(s.phoneNumber.value)
            .setEmail(s.email?.value ?: "")

        s.bankAccounts
            .firstOrNull{ it.isPrimary && it.purpose == BankAccountPurpose.DISBURSEMENT }
            ?.let { acc ->
                builder.primaryDisbursementAccount = PrimaryDisbursementAccount.newBuilder()
                    .setBankCode(acc.bankCode.value)
                    .setAgency(acc.agency.value)
                    .setAccountNumber(acc.accountNumber)
                    .setAccountDigit(acc.accountDigit ?: "")
                    .setAccountType(acc.accountType.toDatabaseValue())
                    .build()
            }
        return builder.build()
    }

    private val log = LoggerFactory.getLogger(javaClass)

    private fun sendError(observer: StreamObserver<*>, status: Status, code: String) {
        observer.onError(status.withDescription(code).asRuntimeException())
    }

    override fun getCustomer(
        request: GetCustomerRequest,
        responseObserver: StreamObserver<GetCustomerResponse>,
    ) {
        try {
            if (request.tenantId.isBlank()) { sendError(responseObserver, Status.INVALID_ARGUMENT, "tenant_id_required"); return }
            val id = UUID.fromString(request.customerId)
            val tenantId = UUID.fromString(request.tenantId)
            val customer = customerRepository.findById(id, tenantId)
                ?: run { sendError(responseObserver, Status.NOT_FOUND, "customer_not_found"); return }
            responseObserver.onNext(GetCustomerResponse.newBuilder().setCustomer(toSummary(customer)).build())
            responseObserver.onCompleted()
        } catch (ex: IllegalArgumentException) {
            sendError(responseObserver, Status.INVALID_ARGUMENT, "invalid_id")
        } catch (ex: Exception) {
            log.error("getCustomer erro inesperado", ex)
            sendError(responseObserver, Status.INTERNAL, "internal_error")
        }
    }

    override fun getCustomerByCpf(
        request: GetCustomerByCpfRequest,
        responseObserver: StreamObserver<GetCustomerResponse>,
    ) {
        try {
            if (request.tenantId.isBlank()) { sendError(responseObserver, Status.INVALID_ARGUMENT, "tenant_id_required"); return }
            val tenantId = UUID.fromString(request.tenantId)
            val cpf = Cpf(request.cpf)
            val customer = customerRepository.findByCpfAndTenantId(cpf, tenantId)
                ?: run { sendError(responseObserver, Status.NOT_FOUND, "customer_not_found"); return }
            responseObserver.onNext(GetCustomerResponse.newBuilder().setCustomer(toSummary(customer)).build())
            responseObserver.onCompleted()
        } catch (ex: IllegalArgumentException) {
            sendError(responseObserver, Status.INVALID_ARGUMENT, ex.message ?: "invalid_argument")
        } catch (ex: Exception) {
            log.error("getCustomerByCpf erro inesperado", ex)
            sendError(responseObserver, Status.INTERNAL, "internal_error")
        }
    }

    override fun batchGetCustomers(
        request: BatchGetCustomersRequest,
        responseObserver: StreamObserver<BatchGetCustomersResponse>,
    ) {
        try {
            if (request.tenantId.isBlank()) { sendError(responseObserver, Status.INVALID_ARGUMENT, "tenant_id_required"); return }
            val tenantId = UUID.fromString(request.tenantId)
            val ids = request.customerIdsList.map { UUID.fromString(it) }
            val customers = customerRepository.findAllByIds(ids, tenantId)
            responseObserver.onNext(
                BatchGetCustomersResponse.newBuilder()
                    .addAllCustomers(customers.map { toSummary(it) })
                    .build()
            )
            responseObserver.onCompleted()
        } catch (ex: IllegalArgumentException) {
            sendError(responseObserver, Status.INVALID_ARGUMENT, "invalid_id")
        } catch (ex: Exception) {
            log.error("batchGetCustomers erro inesperado", ex)
            sendError(responseObserver, Status.INTERNAL, "internal_error")
        }
    }

    override fun searchCustomers(
        request: SearchCustomersRequest,
        responseObserver: StreamObserver<SearchCustomersResponse>,
    ) {
        try {
            if (request.tenantId.isBlank()) { sendError(responseObserver, Status.INVALID_ARGUMENT, "tenant_id_required"); return }
            val tenantId = UUID.fromString(request.tenantId)
            val limit = if (request.limit <= 0) 10 else minOf(request.limit, 50)
            val customers = customerRepository.searchByName(request.query, tenantId, limit)
            responseObserver.onNext(
                SearchCustomersResponse.newBuilder()
                    .addAllCustomers(customers.map { toSummary(it) })
                    .build()
            )
            responseObserver.onCompleted()
        } catch (ex: IllegalArgumentException) {
            sendError(responseObserver, Status.INVALID_ARGUMENT, "invalid_argument")
        } catch (ex: Exception) {
            log.error("searchCustomers erro inesperado", ex)
            sendError(responseObserver, Status.INTERNAL, "internal_error")
        }
    }

}