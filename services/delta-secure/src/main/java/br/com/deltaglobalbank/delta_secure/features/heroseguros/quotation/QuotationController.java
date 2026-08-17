package br.com.deltaglobalbank.delta_secure.features.heroseguros.quotation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heroseguros/prestamista")
public class QuotationController {

    private final QuotationUseCase quotationUseCase;

    public QuotationController(QuotationUseCase quotationUseCase) {
        this.quotationUseCase = quotationUseCase;
    }

    @PostMapping("/quotation")
    public ResponseEntity<QuotationResponse> quote(@Valid @RequestBody QuotationRequest request) {
        return ResponseEntity.ok(quotationUseCase.execute(request));
    }
}
