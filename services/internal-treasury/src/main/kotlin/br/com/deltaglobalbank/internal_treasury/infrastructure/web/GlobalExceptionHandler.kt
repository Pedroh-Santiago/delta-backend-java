package br.com.deltaglobalbank.internal_treasury.infrastructure.web

import br.com.deltaglobalbank.internal_treasury.domain.account.AccountNotFoundException
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InsufficientBalanceException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    data class ApiErrorResponse(
        val error: String,
        val message: String? = null
    )

    @ExceptionHandler(InsufficientBalanceException::class)
    fun handleInsufficientBalance(ex: InsufficientBalanceException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiErrorResponse(error = "insufficient_balance"))
    }

    @ExceptionHandler(AccountNotFoundException::class)
    fun handleAccountNotFound(ex: AccountNotFoundException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error = "account_not_found"))
    }
}