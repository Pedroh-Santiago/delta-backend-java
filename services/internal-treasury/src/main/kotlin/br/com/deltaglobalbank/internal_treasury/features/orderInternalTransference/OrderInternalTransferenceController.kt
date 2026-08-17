package br.com.deltaglobalbank.internal_treasury.features.orderInternalTransference

import com.github.f4b6a3.uuid.UuidCreator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tef")
class OrderInternalTransferenceController (
    private val orderInternalTransferenceUseCase: OrderInternalTransferenceUseCase
){

    @PostMapping("/order")
    fun orderInternalTransference(
        @RequestBody request: OrderInternalTransferenceRequest
    ) : ResponseEntity<OrderInternalTransferenceResponse> {
        val response = orderInternalTransferenceUseCase.execute(
            request, UuidCreator.getTimeOrderedEpoch()
            )
        return ResponseEntity.ok(response)
    }

}