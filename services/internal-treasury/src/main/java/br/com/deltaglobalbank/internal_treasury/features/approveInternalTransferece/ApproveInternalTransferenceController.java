package br.com.deltaglobalbank.internal_treasury.features.approveInternalTransferece;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tef")
public class ApproveInternalTransferenceController {

    private final ApproveInternalTransferenceUseCase approveInternalTransferenceUseCase;

    public ApproveInternalTransferenceController(
        ApproveInternalTransferenceUseCase approveInternalTransferenceUseCase
    ) {
        this.approveInternalTransferenceUseCase = approveInternalTransferenceUseCase;
    }

    @PostMapping("/approve")
    public ResponseEntity<List<UUID>> approveInternalTransference(
        @RequestBody ApproveInternalTransferenceRequest request
    ) {
        List<UUID> approved = approveInternalTransferenceUseCase.execute(request);
        return ResponseEntity.ok(approved);
    }
}
