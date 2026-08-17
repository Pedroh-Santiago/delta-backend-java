package br.com.deltaglobalbank.internal_treasury.features.approveInternalTransferece

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/tef")
class ApproveInternalTransferenceController (
    private val approveInternalTransferenceUseCase: ApproveInternalTransferenceUseCase
) {
    @PostMapping("/approve")
    fun approveInternalTransference(
        @RequestBody request: ApproveInternalTransferenceRequest
    ) : ResponseEntity<List<UUID>> {
        val approved = approveInternalTransferenceUseCase.execute(request)
        return ResponseEntity.ok(approved)
    }
}