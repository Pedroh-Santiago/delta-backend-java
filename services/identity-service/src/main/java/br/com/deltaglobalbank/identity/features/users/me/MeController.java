package br.com.deltaglobalbank.identity.features.users.me;

import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    private final MeUseCase meUseCase;

    public MeController(MeUseCase meUseCase) {
        this.meUseCase = meUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        MeQuery query = new MeQuery(principal.subject(), principal.principalType());
        return ResponseEntity.ok(meUseCase.execute(query));
    }
}
