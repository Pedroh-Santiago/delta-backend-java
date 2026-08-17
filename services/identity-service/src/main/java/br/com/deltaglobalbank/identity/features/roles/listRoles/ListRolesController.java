package br.com.deltaglobalbank.identity.features.roles.listRoles;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListRolesController {

    private final ListRolesUseCase listRolesUseCase;

    public ListRolesController(ListRolesUseCase listRolesUseCase) {
        this.listRolesUseCase = listRolesUseCase;
    }

    @GetMapping("/admin/roles")
    @PreAuthorize("hasRole('platform.admin') or hasRole('identity.admin')")
    public ResponseEntity<ListRolesResponse> listRoles(
        @AuthenticationPrincipal AuthenticatedPrincipal principal,
        @RequestParam(required = false) Boolean active
    ) {
        ListRolesQuery query = new ListRolesQuery(
            principal.roles().contains("platform.admin"),
            Boolean.TRUE.equals(active)
        );
        return ResponseEntity.ok(listRolesUseCase.execute(query));
    }
}
