package br.com.deltaglobalbank.internal_treasury.features.listInternalTrasference

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tef")
class ListInternalTransferenceController(
    private val useCase: ListInternalTransferenceUseCase
) {
    @GetMapping("/list")
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "waiting") filter: String
    ): ResponseEntity<ListInternalTransferenceResponse> {
        val response = useCase.execute(page, pageSize, filter)
        return ResponseEntity.ok(response)
    }
}