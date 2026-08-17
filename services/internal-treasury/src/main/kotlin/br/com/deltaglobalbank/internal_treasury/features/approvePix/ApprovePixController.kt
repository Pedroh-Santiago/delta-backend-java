package br.com.deltaglobalbank.internal_treasury.features.approvePix

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/payments")
class ApprovePixController (
    private val approvePixUseCase: ApprovePixUseCase
) {
    @PostMapping("/approve")
    fun approvePix(
        @RequestBody request: ApprovePixRequest
    ) : ResponseEntity<List<UUID>> {
        val approved = approvePixUseCase.execute(request)
        return ResponseEntity.ok(approved)
    }
}