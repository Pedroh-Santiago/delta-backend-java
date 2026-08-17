package br.com.deltaglobalbank.products.infrastructure.grpc

import br.com.deltaglobalbank.products.TestcontainersConfiguration
import br.com.deltaglobalbank.products.domain.product.AgreementName
import br.com.deltaglobalbank.products.domain.product.DisplayName
import br.com.deltaglobalbank.products.domain.product.Product
import br.com.deltaglobalbank.products.domain.product.ProductType
import br.com.deltaglobalbank.products.grpc.GetProductByIdRequest
import br.com.deltaglobalbank.products.grpc.ListActiveProductsByTypeRequest
import br.com.deltaglobalbank.products.grpc.ListProductsRequest
import br.com.deltaglobalbank.products.grpc.ListProductsResponse
import br.com.deltaglobalbank.products.grpc.ProductResponse
import br.com.deltaglobalbank.products.infrastructure.persistence.mappers.toEntity
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
@Transactional
class ProductsInternalServiceTests {

    @Autowired
    lateinit var service: ProductsInternalServiceImpl
    @Autowired lateinit var jpaProductRepository: JpaProductRepository

    private val tenantId = UUID.randomUUID()

    private class CaptureObserver<T> : StreamObserver<T> {
        var value: T? = null; var error: Throwable? = null; var completed = false
        override fun onNext(v: T) { value = v }
        override fun onError(t: Throwable) { error = t }
        override fun onCompleted() { completed = true }
    }

    private fun seed(
        tenant: UUID = tenantId,
        agreementName: String = "INSS",
        active: Boolean = true,
    ): UUID {
        val id = UUID.randomUUID()
        val now = Instant.now()
        val product = Product(
            id = id, tenantId = tenant, type = ProductType.LENDING,
            agreementName = AgreementName(agreementName), displayName = DisplayName("Produto"),
            minMonthlyRate = BigDecimal("1.5"), maxMonthlyRate = BigDecimal("3.0"),
            minMonths = 12, maxMonths = 60,
            minAmount = BigDecimal("1000.00"), maxAmount = BigDecimal("50000.00"),
            commissionRate = BigDecimal("2.0"), active = active,
            createdAt = now, updatedAt = now, createdBy = null, updatedBy = null,
        )
        jpaProductRepository.save(product.toEntity())
        return id
    }

    private fun req(id: UUID, tenant: UUID): GetProductByIdRequest =
        GetProductByIdRequest.newBuilder()
            .setProductId(id.toString())
            .setTenantId(tenant.toString())
            .build()

    @Test
    fun `getProductById returns product of own tenant`() {
        val id = seed(tenantId)
        val obs = CaptureObserver<ProductResponse>()
        service.getProductById(req(id, tenantId), obs)
        assertEquals(id.toString(), obs.value!!.product.id)
        assertEquals("LENDING", obs.value!!.product.type)
    }

    @Test fun `getProductById NOT_FOUND for another tenant`() {
        val id = seed(tenantId)
        val obs = CaptureObserver<ProductResponse>()
        service.getProductById(req(id, UUID.randomUUID()), obs)
        assertNull(obs.value)
        assertEquals(Status.Code.NOT_FOUND, Status.fromThrowable(obs.error!!).code)
    }

    @Test fun `getProductById INVALID_ARGUMENT for malformed id`() {
        val obs = CaptureObserver<ProductResponse>()
        service.getProductById(
            GetProductByIdRequest.newBuilder().setProductId("nao-uuid").setTenantId(tenantId.toString()).build(), obs)
        assertEquals(Status.Code.INVALID_ARGUMENT, Status.fromThrowable(obs.error!!).code)
    }

    @Test fun `getProductById INVALID_ARGUMENT when tenant blank`() {
        val obs = CaptureObserver<ProductResponse>()
        service.getProductById(
            GetProductByIdRequest.newBuilder().setProductId(UUID.randomUUID().toString()).setTenantId("").build(), obs)
        assertEquals(Status.Code.INVALID_ARGUMENT, Status.fromThrowable(obs.error!!).code)
    }

    @Test fun `listProducts returns all of tenant when no filter`() {
        seed(tenantId, "INSS", active = true)
        seed(tenantId, "SIAPE", active = false)
        val obs = CaptureObserver<ListProductsResponse>()
        service.listProducts(ListProductsRequest.newBuilder().setTenantId(tenantId.toString()).build(), obs)
        assertEquals(2, obs.value!!.productsList.size)
    }

    @Test fun `listProducts filters by active`() {
        seed(tenantId, "INSS", active = true)
        seed(tenantId, "SIAPE", active = false)
        val obs = CaptureObserver<ListProductsResponse>()
        service.listProducts(
            ListProductsRequest.newBuilder().setTenantId(tenantId.toString()).setActive(true).build(), obs)
        assertEquals(1, obs.value!!.productsList.size)
        assertTrue(obs.value!!.productsList.all { it.active })
    }

    @Test fun `listProducts filters by agreementName partial and case-insensitive`() {
        seed(tenantId, "INSS"); seed(tenantId, "SIAPE")
        val obs = CaptureObserver<ListProductsResponse>()
        service.listProducts(
            ListProductsRequest.newBuilder().setTenantId(tenantId.toString()).setAgreementName("ins").build(), obs)
        assertEquals(1, obs.value!!.productsList.size)
    }

    @Test fun `listProducts is scoped by tenant`() {
        seed(tenantId, "INSS")
        val obs = CaptureObserver<ListProductsResponse>()
        service.listProducts(ListProductsRequest.newBuilder().setTenantId(UUID.randomUUID().toString()).build(), obs)
        assertTrue(obs.value!!.productsList.isEmpty())
    }

    @Test fun `listActiveProductsByType returns only active of the type`() {
        seed(tenantId, "INSS", active = true)
        seed(tenantId, "SIAPE", active = false)
        val obs = CaptureObserver<ListProductsResponse>()
        service.listActiveProductsByType(
            ListActiveProductsByTypeRequest.newBuilder().setTenantId(tenantId.toString()).setType("LENDING").build(), obs)
        assertEquals(1, obs.value!!.productsList.size)
        assertTrue(obs.value!!.productsList.all { it.active })
    }

    @Test fun `listActiveProductsByType is scoped by tenant`() {
        seed(tenantId, "INSS", active = true)
        val obs = CaptureObserver<ListProductsResponse>()
        service.listActiveProductsByType(
            ListActiveProductsByTypeRequest.newBuilder().setTenantId(UUID.randomUUID().toString()).setType("LENDING").build(), obs)
        assertTrue(obs.value!!.productsList.isEmpty())
    }

    @Test fun `listActiveProductsByType INVALID_ARGUMENT for unknown type`() {
        val obs = CaptureObserver<ListProductsResponse>()
        service.listActiveProductsByType(
            ListActiveProductsByTypeRequest.newBuilder().setTenantId(tenantId.toString()).setType("XPTO").build(), obs)
        assertEquals(Status.Code.INVALID_ARGUMENT, Status.fromThrowable(obs.error!!).code)
    }
}