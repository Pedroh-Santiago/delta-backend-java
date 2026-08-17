package br.com.deltaglobalbank.products.infrastructure.grpc

import br.com.deltaglobalbank.products.domain.product.Product
import br.com.deltaglobalbank.products.domain.product.ProductRepository
import br.com.deltaglobalbank.products.domain.product.ProductType
import br.com.deltaglobalbank.products.grpc.GetProductByIdRequest
import br.com.deltaglobalbank.products.grpc.ListActiveProductsByTypeRequest
import br.com.deltaglobalbank.products.grpc.ListProductsRequest
import br.com.deltaglobalbank.products.grpc.ListProductsResponse
import br.com.deltaglobalbank.products.grpc.ProductResponse
import br.com.deltaglobalbank.products.grpc.ProductSummary
import br.com.deltaglobalbank.products.grpc.ProductsInternalServiceGrpc
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProductsInternalServiceImpl(
    private val repository: ProductRepository
) : ProductsInternalServiceGrpc.ProductsInternalServiceImplBase() {

    private fun toSummary(p: Product): ProductSummary {
        val s = p.snapshot()
        return ProductSummary.newBuilder()
            .setId(s.id.toString())
            .setTenantId(s.tenantId.toString())
            .setType(s.type.toDatabaseValue())
            .setAgreementName(s.agreementName.value)
            .setMinMonthlyRate(s.minMonthlyRate.toPlainString())
            .setMaxMonthlyRate(s.maxMonthlyRate.toPlainString())
            .setMinMonths(s.minMonths)
            .setMaxMonths(s.maxMonths)
            .setMinAmount(s.minAmount.toPlainString())
            .setMaxAmount(s.maxAmount.toPlainString())
            .setCommissionRate(s.commissionRate?.toPlainString() ?: "")
            .setActive(s.active)
            .setCreatedAt(s.createdAt.toString())
            .setUpdatedAt(s.updatedAt.toString())
            .build()
    }

    private val log = LoggerFactory.getLogger(javaClass)

    private fun sendError(observer: StreamObserver<*>, status: Status, code: String) {
        observer.onError(status.withDescription(code).asRuntimeException())
    }

    override fun getProductById(request: GetProductByIdRequest, obs: StreamObserver<ProductResponse>) {
        try {
            if (request.tenantId.isBlank()) { sendError(obs, Status.INVALID_ARGUMENT, "tenant_id_required"); return }
            val product = repository.findById(UUID.fromString(request.productId), UUID.fromString(request.tenantId))
                ?: run { sendError(obs, Status.NOT_FOUND, "product_not_found"); return }
            obs.onNext(ProductResponse.newBuilder().setProduct(toSummary(product)).build()); obs.onCompleted()
        } catch (e: IllegalArgumentException) { sendError(obs, Status.INVALID_ARGUMENT, "invalid_id") }
        catch (e: Exception) { log.error("getProductById", e); sendError(obs, Status.INTERNAL, "internal_error") }
    }

    override fun listProducts(request: ListProductsRequest, obs: StreamObserver<ListProductsResponse>) {
        try {
            if (request.tenantId.isBlank()) { sendError(obs, Status.INVALID_ARGUMENT, "tenant_id_required"); return }
            val tenantId = UUID.fromString(request.tenantId)
            val type = request.type.ifBlank { null }?.let { ProductType.fromDatabaseValue(it) }
            val active = if (request.hasActive()) request.active else null
            val agreement = request.agreementName.ifBlank { null }
            val items = repository.findProducts(tenantId, type, active, agreement)
            obs.onNext(ListProductsResponse.newBuilder().addAllProducts(items.map { toSummary(it) }).build())
            obs.onCompleted()
        } catch (e: IllegalArgumentException) { sendError(obs, Status.INVALID_ARGUMENT, "invalid_argument") }
        catch (e: Exception) { log.error("listProducts", e); sendError(obs, Status.INTERNAL, "internal_error") }
    }

    override fun listActiveProductsByType(request: ListActiveProductsByTypeRequest, obs: StreamObserver<ListProductsResponse>) {
        try {
            if (request.tenantId.isBlank()) { sendError(obs, Status.INVALID_ARGUMENT, "tenant_id_required"); return }
            val tenantId = UUID.fromString(request.tenantId)
            val type = ProductType.fromDatabaseValue(request.type)
            val items = repository.findProducts(tenantId, type, active = true, agreementName = null)
            obs.onNext(ListProductsResponse.newBuilder().addAllProducts(items.map { toSummary(it) }).build())
            obs.onCompleted()
        } catch (e: IllegalArgumentException) { sendError(obs, Status.INVALID_ARGUMENT, "invalid_argument") }
        catch (e: Exception) { log.error("listActiveProductsByType", e); sendError(obs, Status.INTERNAL, "internal_error") }
    }
}