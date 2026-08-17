package br.com.deltaglobalbank.internal_treasury.features.approvePix;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class ApprovePixController {

    private final ApprovePixUseCase approvePixUseCase;

    public ApprovePixController(ApprovePixUseCase approvePixUseCase) {
        this.approvePixUseCase = approvePixUseCase;
    }

    @PostMapping("/approve")
    public ResponseEntity<List<UUID>> approvePix(
        @RequestBody ApprovePixRequest request
    ) {
        List<UUID> approved = approvePixUseCase.execute(request);
        return ResponseEntity.ok(approved);
    }
}
