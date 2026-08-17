package br.com.deltaglobalbank.products.features.lending.createLendingProduct;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class CreateLendingProductController {

    private final CreateLendingProductUseCase useCase;

    public CreateLendingProductController(CreateLendingProductUseCase useCase) {
        this.useCase = useCase;
    }

    @PreAuthorize("hasAnyRole('products.lending.create','products.lending.admin')")
    @PostMapping("/products/lending")
    public ResponseEntity<CreateLendingProductResponse> create(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @Valid @RequestBody CreateLendingProductRequest request
    ) {
        CreateLendingProductCommand command = new CreateLendingProductCommand(
            principal.tenantId(),
            principal.subject(),
            request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.execute(command));
    }
}
