package br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tef")
public class OrderInternalTransferenceController {

    private final OrderInternalTransferenceUseCase orderInternalTransferenceUseCase;

    public OrderInternalTransferenceController(
        OrderInternalTransferenceUseCase orderInternalTransferenceUseCase
    ) {
        this.orderInternalTransferenceUseCase = orderInternalTransferenceUseCase;
    }

    @PostMapping("/order")
    public ResponseEntity<OrderInternalTransferenceResponse> orderInternalTransference(
        @RequestBody OrderInternalTransferenceRequest request
    ) {
        OrderInternalTransferenceResponse response = orderInternalTransferenceUseCase.execute(
            request, UuidCreator.getTimeOrderedEpoch()
        );
        return ResponseEntity.ok(response);
    }
}
