package br.com.deltaglobalbank.internal_treasury.features.listInternalTrasference;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tef")
public class ListInternalTransferenceController {

    private final ListInternalTransferenceUseCase useCase;

    public ListInternalTransferenceController(ListInternalTransferenceUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/list")
    public ResponseEntity<ListInternalTransferenceResponse> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "waiting") String filter
    ) {
        ListInternalTransferenceResponse response = useCase.execute(page, pageSize, filter);
        return ResponseEntity.ok(response);
    }
}
