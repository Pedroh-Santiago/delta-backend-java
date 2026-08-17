package br.com.deltaglobalbank.internal_treasury.features.makePix

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments")
class MakePixController (
    private val makePixUseCase: MakePixUseCase
) {
    @PostMapping("/orderPix")
    fun makePix(
        @RequestBody request: MakePixRequest
    ) : ResponseEntity<MakePixResponse> {
        val response = makePixUseCase.execute(request)
        return ResponseEntity.ok(response)
    }
}