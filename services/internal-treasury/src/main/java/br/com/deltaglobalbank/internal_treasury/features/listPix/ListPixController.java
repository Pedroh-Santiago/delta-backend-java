package br.com.deltaglobalbank.internal_treasury.features.listPix;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class ListPixController {

    private final ListPixUseCase useCase;

    public ListPixController(ListPixUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/list")
    public ResponseEntity<ListPixResponse> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "waiting") String filter
    ) {
        ListPixResponse response = useCase.execute(page, pageSize, filter);
        return ResponseEntity.ok(response);
    }
}
