package br.com.deltaglobalbank.internal_treasury.features.makePix;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class MakePixController {

    private final MakePixUseCase makePixUseCase;

    public MakePixController(MakePixUseCase makePixUseCase) {
        this.makePixUseCase = makePixUseCase;
    }

    @PostMapping("/orderPix")
    public ResponseEntity<MakePixResponse> makePix(
        @RequestBody MakePixRequest request
    ) {
        MakePixResponse response = makePixUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}
