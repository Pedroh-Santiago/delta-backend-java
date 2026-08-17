package br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeys;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ListSigningKeyController {

    private final ListSigningKeysUseCase listSigningKeysUseCase;

    public ListSigningKeyController(ListSigningKeysUseCase listSigningKeysUseCase) {
        this.listSigningKeysUseCase = listSigningKeysUseCase;
    }

    @GetMapping("/admin/signing-keys")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<ListSigningKeysResponse> listSigningKey() {
        return ResponseEntity.ok(listSigningKeysUseCase.execute());
    }
}
