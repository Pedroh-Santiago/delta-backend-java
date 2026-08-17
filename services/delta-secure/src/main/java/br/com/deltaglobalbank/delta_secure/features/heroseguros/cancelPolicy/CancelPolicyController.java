package br.com.deltaglobalbank.delta_secure.features.heroseguros.cancelPolicy;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heroseguros/prestamista")
public class CancelPolicyController {

    private final CancelPolicyUseCase cancelPolicyUseCase;

    public CancelPolicyController(CancelPolicyUseCase cancelPolicyUseCase) {
        this.cancelPolicyUseCase = cancelPolicyUseCase;
    }

    @PostMapping("/policies/cancel")
    public ResponseEntity<CancelPolicyResponse> cancel(@Valid @RequestBody CancelPolicyRequest request) {
        return ResponseEntity.ok(cancelPolicyUseCase.execute(request));
    }
}
