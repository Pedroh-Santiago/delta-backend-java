package br.com.deltaglobalbank.identity.features.tenants.listTenants;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListTenantsController {

    private final ListTenantsUseCase listTenantsUseCase;

    public ListTenantsController(ListTenantsUseCase listTenantsUseCase) {
        this.listTenantsUseCase = listTenantsUseCase;
    }

    @GetMapping("/admin/tenants")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<ListTenantsResponse> listTenants(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        ListTenantsQuery query = new ListTenantsQuery(page, size);
        return ResponseEntity.ok(listTenantsUseCase.execute(query));
    }
}
