package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heroseguros/prestamista")
public class IssuePolicyController {

    private final IssuePolicyUseCase issuePolicyUseCase;
    private final TermoAdesaoRegenerationService termoAdesaoRegenerationService;

    public IssuePolicyController(
        IssuePolicyUseCase issuePolicyUseCase,
        TermoAdesaoRegenerationService termoAdesaoRegenerationService
    ) {
        this.issuePolicyUseCase = issuePolicyUseCase;
        this.termoAdesaoRegenerationService = termoAdesaoRegenerationService;
    }

    @PostMapping("/policies")
    public ResponseEntity<IssuePolicyResponse> issuePolicy(@Valid @RequestBody IssuePolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issuePolicyUseCase.execute(request));
    }

    @GetMapping("/policies/{ticket}/termo-adesao")
    public ResponseEntity<byte[]> baixarTermoAdesao(@PathVariable String ticket) {
        byte[] content = termoAdesaoRegenerationService.regenerate(ticket);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "termo-adesao-seguro-" + ticket + ".pdf");

        return ResponseEntity.ok().headers(headers).body(content);
    }
}
