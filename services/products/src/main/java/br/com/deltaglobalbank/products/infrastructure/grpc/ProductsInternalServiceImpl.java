package br.com.deltaglobalbank.products.infrastructure.grpc;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.products.domain.product.Product;
import br.com.deltaglobalbank.products.domain.product.ProductRepository;
import br.com.deltaglobalbank.products.domain.product.ProductSnapshot;
import br.com.deltaglobalbank.products.domain.product.ProductType;
import br.com.deltaglobalbank.products.grpc.GetProductByIdRequest;
import br.com.deltaglobalbank.products.grpc.ListActiveProductsByTypeRequest;
import br.com.deltaglobalbank.products.grpc.ListProductsRequest;
import br.com.deltaglobalbank.products.grpc.ListProductsResponse;
import br.com.deltaglobalbank.products.grpc.ProductResponse;
import br.com.deltaglobalbank.products.grpc.ProductSummary;
import br.com.deltaglobalbank.products.grpc.ProductsInternalServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductsInternalServiceImpl extends ProductsInternalServiceGrpc.ProductsInternalServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ProductsInternalServiceImpl.class);

    private final ProductRepository repository;

    public ProductsInternalServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    private ProductSummary toSummary(Product p) {
        ProductSnapshot s = p.snapshot();
        return ProductSummary.newBuilder()
            .setId(s.id().toString())
            .setTenantId(s.tenantId().toString())
            .setType(s.type().toDatabaseValue())
            .setAgreementName(s.agreementName().value())
            .setMinMonthlyRate(s.minMonthlyRate().toPlainString())
            .setMaxMonthlyRate(s.maxMonthlyRate().toPlainString())
            .setMinMonths(s.minMonths())
            .setMaxMonths(s.maxMonths())
            .setMinAmount(s.minAmount().toPlainString())
            .setMaxAmount(s.maxAmount().toPlainString())
            .setCommissionRate(s.commissionRate() != null ? s.commissionRate().toPlainString() : "")
            .setActive(s.active())
            .setCreatedAt(s.createdAt().toString())
            .setUpdatedAt(s.updatedAt().toString())
            .build();
    }

    private void sendError(StreamObserver<?> observer, Status status, String code) {
        observer.onError(status.withDescription(code).asRuntimeException());
    }

    @Override
    public void getProductById(GetProductByIdRequest request, StreamObserver<ProductResponse> obs) {
        try {
            if (request.getTenantId().isBlank()) {
                sendError(obs, Status.INVALID_ARGUMENT, "tenant_id_required");
                return;
            }
            Product product = repository.findById(
                UUID.fromString(request.getProductId()), UUID.fromString(request.getTenantId()));
            if (product == null) {
                sendError(obs, Status.NOT_FOUND, "product_not_found");
                return;
            }
            obs.onNext(ProductResponse.newBuilder().setProduct(toSummary(product)).build());
            obs.onCompleted();
        } catch (IllegalArgumentException e) {
            sendError(obs, Status.INVALID_ARGUMENT, "invalid_id");
        } catch (Exception e) {
            log.error("getProductById", e);
            sendError(obs, Status.INTERNAL, "internal_error");
        }
    }

    @Override
    public void listProducts(ListProductsRequest request, StreamObserver<ListProductsResponse> obs) {
        try {
            if (request.getTenantId().isBlank()) {
                sendError(obs, Status.INVALID_ARGUMENT, "tenant_id_required");
                return;
            }
            UUID tenantId = UUID.fromString(request.getTenantId());
            ProductType type = request.getType().isBlank() ? null : ProductType.fromDatabaseValue(request.getType());
            Boolean active = request.hasActive() ? request.getActive() : null;
            String agreement = request.getAgreementName().isBlank() ? null : request.getAgreementName();
            List<Product> items = repository.findProducts(tenantId, type, active, agreement);
            obs.onNext(ListProductsResponse.newBuilder()
                .addAllProducts(items.stream().map(this::toSummary).toList())
                .build());
            obs.onCompleted();
        } catch (IllegalArgumentException e) {
            sendError(obs, Status.INVALID_ARGUMENT, "invalid_argument");
        } catch (Exception e) {
            log.error("listProducts", e);
            sendError(obs, Status.INTERNAL, "internal_error");
        }
    }

    @Override
    public void listActiveProductsByType(ListActiveProductsByTypeRequest request, StreamObserver<ListProductsResponse> obs) {
        try {
            if (request.getTenantId().isBlank()) {
                sendError(obs, Status.INVALID_ARGUMENT, "tenant_id_required");
                return;
            }
            UUID tenantId = UUID.fromString(request.getTenantId());
            ProductType type = ProductType.fromDatabaseValue(request.getType());
            List<Product> items = repository.findProducts(tenantId, type, true, null);
            obs.onNext(ListProductsResponse.newBuilder()
                .addAllProducts(items.stream().map(this::toSummary).toList())
                .build());
            obs.onCompleted();
        } catch (IllegalArgumentException e) {
            sendError(obs, Status.INVALID_ARGUMENT, "invalid_argument");
        } catch (Exception e) {
            log.error("listActiveProductsByType", e);
            sendError(obs, Status.INTERNAL, "internal_error");
        }
    }
}
