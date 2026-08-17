package br.com.deltaglobalbank.delta_secure.features.heroseguros.getPolicy;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heroseguros/prestamista")
public class GetPolicyController {

    private final GetPolicyUseCase getPolicyUseCase;

    public GetPolicyController(GetPolicyUseCase getPolicyUseCase) {
        this.getPolicyUseCase = getPolicyUseCase;
    }

    @PostMapping("/policies/get")
    public ResponseEntity<GetPolicyResponse> getPolicy(@Valid @RequestBody GetPolicyRequest request) {
        return ResponseEntity.ok(getPolicyUseCase.execute(request));
    }
}
