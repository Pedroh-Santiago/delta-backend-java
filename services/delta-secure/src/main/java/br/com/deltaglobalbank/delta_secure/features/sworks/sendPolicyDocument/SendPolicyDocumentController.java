package br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument;

import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchResult;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy.TermoAdesaoRegenerationService;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksAccessDenied;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksAuthenticationFailed;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksDocumentRejected;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksInvalidDocument;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksInvalidResponse;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksNotConfigured;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksProcessNotFound;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksTokenExpired;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksUnavailable;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SendPolicyDocumentController {

    private static final String MISMATCH = "SWorksDocumentMismatch";

    private final SendPolicyDocumentToSWorksUseCase sendPolicyDocumentToSWorksUseCase;
    private final TermoAdesaoRegenerationService termoAdesaoRegenerationService;

    public SendPolicyDocumentController(
        SendPolicyDocumentToSWorksUseCase sendPolicyDocumentToSWorksUseCase,
        TermoAdesaoRegenerationService termoAdesaoRegenerationService
    ) {
        this.sendPolicyDocumentToSWorksUseCase = sendPolicyDocumentToSWorksUseCase;
        this.termoAdesaoRegenerationService = termoAdesaoRegenerationService;
    }

    @PostMapping("/heroseguros/prestamista/policies/{ticket}/termo-adesao/sworks")
    public ResponseEntity<SendPolicyDocumentResponse> send(
        @PathVariable String ticket,
        @Valid @RequestBody SendPolicyDocumentRequest request
    ) {
        byte[] documentBytes = termoAdesaoRegenerationService.regenerate(ticket);
        DocumentDispatchResult result = sendPolicyDocumentToSWorksUseCase.execute(
            new SendPolicyDocumentCommand(
                request.idProposal(),
                ticket,
                request.identificadorProcesso(),
                documentBytes
            )
        );

        return ResponseEntity.status(statusDe(result)).body(SendPolicyDocumentMappers.toResponse(result));
    }

    private HttpStatus statusDe(DocumentDispatchResult result) {
        if (result instanceof DocumentDispatchResult.Success) {
            return HttpStatus.OK;
        }

        DocumentDispatchResult.Failure failure = (DocumentDispatchResult.Failure) result;
        String error = failure.error();

        if (error.equals(SWorksInvalidDocument.class.getSimpleName())
            || error.equals(SWorksDocumentRejected.class.getSimpleName())) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }

        if (error.equals(SWorksProcessNotFound.class.getSimpleName())) {
            return HttpStatus.NOT_FOUND;
        }

        if (error.equals(SWorksUnavailable.class.getSimpleName())) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        if (error.equals(SWorksAuthenticationFailed.class.getSimpleName())
            || error.equals(SWorksAccessDenied.class.getSimpleName())
            || error.equals(SWorksNotConfigured.class.getSimpleName())
            || error.equals(SWorksInvalidResponse.class.getSimpleName())
            || error.equals(SWorksTokenExpired.class.getSimpleName())
            || error.equals(MISMATCH)) {
            return HttpStatus.BAD_GATEWAY;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
