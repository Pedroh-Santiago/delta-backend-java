package br.com.deltaglobalbank.products.infrastructure.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.deltaglobalbank.products.TestcontainersConfiguration;
import br.com.deltaglobalbank.products.domain.product.AgreementName;
import br.com.deltaglobalbank.products.domain.product.DisplayName;
import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductType;
import br.com.deltaglobalbank.products.grpc.GetProductByIdRequest;
import br.com.deltaglobalbank.products.grpc.ListActiveProductsByTypeRequest;
import br.com.deltaglobalbank.products.grpc.ListProductsRequest;
import br.com.deltaglobalbank.products.grpc.ListProductsResponse;
import br.com.deltaglobalbank.products.grpc.ProductResponse;
import br.com.deltaglobalbank.products.infrastructure.persistence.mappers.ProductMapper;
import br.com.deltaglobalbank.products.infrastructure.persistence.repositories.JpaProductRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class ProductsInternalServiceTests {

    @Autowired
    ProductsInternalServiceImpl service;
    @Autowired
    JpaProductRepository jpaProductRepository;

    private final UUID tenantId = UUID.randomUUID();

    private static class CaptureObserver<T> implements StreamObserver<T> {
        T value = null;
        Throwable error = null;
        boolean completed = false;

        @Override
        public void onNext(T v) {
            value = v;
        }

        @Override
        public void onError(Throwable t) {
            error = t;
        }

        @Override
        public void onCompleted() {
            completed = true;
        }
    }

    private UUID seed(UUID tenant, String agreementName, boolean active) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Product product = new Product(
            id, tenant, ProductType.LENDING,
            new AgreementName(agreementName), new DisplayName("Produto"),
            new BigDecimal("1.5"), new BigDecimal("3.0"),
            12, 60,
            new BigDecimal("1000.00"), new BigDecimal("50000.00"),
            new BigDecimal("2.0"), active,
            now, now, null, null
        );
        jpaProductRepository.save(ProductMapper.toEntity(product));
        return id;
    }

    private UUID seed(UUID tenant) {
        return seed(tenant, "INSS", true);
    }

    private UUID seed(UUID tenant, String agreementName) {
        return seed(tenant, agreementName, true);
    }

    private GetProductByIdRequest req(UUID id, UUID tenant) {
        return GetProductByIdRequest.newBuilder()
            .setProductId(id.toString())
            .setTenantId(tenant.toString())
            .build();
    }

    @Test
    void getProductByIdReturnsProductOfOwnTenant() {
        UUID id = seed(tenantId);
        CaptureObserver<ProductResponse> obs = new CaptureObserver<>();
        service.getProductById(req(id, tenantId), obs);
        assertEquals(id.toString(), obs.value.getProduct().getId());
        assertEquals("LENDING", obs.value.getProduct().getType());
    }

    @Test
    void getProductByIdNotFoundForAnotherTenant() {
        UUID id = seed(tenantId);
        CaptureObserver<ProductResponse> obs = new CaptureObserver<>();
        service.getProductById(req(id, UUID.randomUUID()), obs);
        assertNull(obs.value);
        assertEquals(Status.Code.NOT_FOUND, Status.fromThrowable(obs.error).getCode());
    }

    @Test
    void getProductByIdInvalidArgumentForMalformedId() {
        CaptureObserver<ProductResponse> obs = new CaptureObserver<>();
        service.getProductById(
            GetProductByIdRequest.newBuilder().setProductId("nao-uuid").setTenantId(tenantId.toString()).build(), obs);
        assertEquals(Status.Code.INVALID_ARGUMENT, Status.fromThrowable(obs.error).getCode());
    }

    @Test
    void getProductByIdInvalidArgumentWhenTenantBlank() {
        CaptureObserver<ProductResponse> obs = new CaptureObserver<>();
        service.getProductById(
            GetProductByIdRequest.newBuilder().setProductId(UUID.randomUUID().toString()).setTenantId("").build(), obs);
        assertEquals(Status.Code.INVALID_ARGUMENT, Status.fromThrowable(obs.error).getCode());
    }

    @Test
    void listProductsReturnsAllOfTenantWhenNoFilter() {
        seed(tenantId, "INSS", true);
        seed(tenantId, "SIAPE", false);
        CaptureObserver<ListProductsResponse> obs = new CaptureObserver<>();
        service.listProducts(ListProductsRequest.newBuilder().setTenantId(tenantId.toString()).build(), obs);
        assertEquals(2, obs.value.getProductsList().size());
    }

    @Test
    void listProductsFiltersByActive() {
        seed(tenantId, "INSS", true);
        seed(tenantId, "SIAPE", false);
        CaptureObserver<ListProductsResponse> obs = new CaptureObserver<>();
        service.listProducts(
            ListProductsRequest.newBuilder().setTenantId(tenantId.toString()).setActive(true).build(), obs);
        assertEquals(1, obs.value.getProductsList().size());
        assertTrue(obs.value.getProductsList().stream().allMatch(p -> p.getActive()));
    }

    @Test
    void listProductsFiltersByAgreementNamePartialAndCaseInsensitive() {
        seed(tenantId, "INSS");
        seed(tenantId, "SIAPE");
        CaptureObserver<ListProductsResponse> obs = new CaptureObserver<>();
        service.listProducts(
            ListProductsRequest.newBuilder().setTenantId(tenantId.toString()).setAgreementName("ins").build(), obs);
        assertEquals(1, obs.value.getProductsList().size());
    }

    @Test
    void listProductsIsScopedByTenant() {
        seed(tenantId, "INSS");
        CaptureObserver<ListProductsResponse> obs = new CaptureObserver<>();
        service.listProducts(ListProductsRequest.newBuilder().setTenantId(UUID.randomUUID().toString()).build(), obs);
        assertTrue(obs.value.getProductsList().isEmpty());
    }

    @Test
    void listActiveProductsByTypeReturnsOnlyActiveOfTheType() {
        seed(tenantId, "INSS", true);
        seed(tenantId, "SIAPE", false);
        CaptureObserver<ListProductsResponse> obs = new CaptureObserver<>();
        service.listActiveProductsByType(
            ListActiveProductsByTypeRequest.newBuilder().setTenantId(tenantId.toString()).setType("LENDING").build(), obs);
        assertEquals(1, obs.value.getProductsList().size());
        assertTrue(obs.value.getProductsList().stream().allMatch(p -> p.getActive()));
    }

    @Test
    void listActiveProductsByTypeIsScopedByTenant() {
        seed(tenantId, "INSS", true);
        CaptureObserver<ListProductsResponse> obs = new CaptureObserver<>();
        service.listActiveProductsByType(
            ListActiveProductsByTypeRequest.newBuilder().setTenantId(UUID.randomUUID().toString()).setType("LENDING").build(), obs);
        assertTrue(obs.value.getProductsList().isEmpty());
    }

    @Test
    void listActiveProductsByTypeInvalidArgumentForUnknownType() {
        CaptureObserver<ListProductsResponse> obs = new CaptureObserver<>();
        service.listActiveProductsByType(
            ListActiveProductsByTypeRequest.newBuilder().setTenantId(tenantId.toString()).setType("XPTO").build(), obs);
        assertEquals(Status.Code.INVALID_ARGUMENT, Status.fromThrowable(obs.error).getCode());
    }
}
