package br.com.deltaglobalbank.internal_treasury.features.listPix

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments")
class ListPixController (
    private val useCase: ListPixUseCase
) {
    @GetMapping("/list")
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "waiting") filter: String
    ): ResponseEntity<ListPixResponse>{
        val response = useCase.execute(page, pageSize, filter)
        return ResponseEntity.ok(response)
    }
}