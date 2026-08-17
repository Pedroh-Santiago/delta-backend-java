package br.com.deltaglobalbank.customers.infrastructure.web

import br.com.deltaglobalbank.customers.domain.customer.BankAccountNotFound
import br.com.deltaglobalbank.customers.domain.customer.CpfAlreadyExists
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound
import br.com.deltaglobalbank.customers.domain.customer.DuplicateBankAccount
import br.com.deltaglobalbank.customers.domain.customer.DuplicatePersonalDocument
import br.com.deltaglobalbank.customers.domain.customer.DuplicatePrimaryAccountForPurpose
import br.com.deltaglobalbank.customers.domain.customer.SubaggregateDoesNotBelongToCustomer
import br.com.deltaglobalbank.customers.infrastructure.web.responses.ApiErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiErrorResponse> {
        log.error("Erro não tratado", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse(error = "internal_error"))
    }

    @ExceptionHandler(CustomerNotFound::class)
    fun handleCustomerNotFounf(ex: CustomerNotFound): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error = "customer_not_found"))
    }

    @ExceptionHandler(BankAccountNotFound::class)
    fun handleBankAccountNotFound(ex: BankAccountNotFound ): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error = "bank_account_not_found"))
    }

    @ExceptionHandler(CpfAlreadyExists ::class)
    fun handleCpfAlreadyExists(ex: CpfAlreadyExists): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(error = "cpf_already_exists"))
    }

    @ExceptionHandler(DuplicatePrimaryAccountForPurpose::class)
    fun handleDuplicatePrimaryAccountForPurpose(ex: DuplicatePrimaryAccountForPurpose  ): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = "multiple_primary_per_purpose"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = ex.message ?: "invalid_argument"))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = "malformed_request"))

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleDenied(ex: AuthorizationDeniedException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponse(error = "denied"))

    @ExceptionHandler(SubaggregateDoesNotBelongToCustomer::class)
    fun handleSubaggregate(ex: SubaggregateDoesNotBelongToCustomer): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = "subaggregate_does_not_belong_to_customer"))

    @ExceptionHandler(DuplicateBankAccount::class)
    fun handleDuplicateAccount(ex: DuplicateBankAccount): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = "duplicate_bank_account"))

    @ExceptionHandler(DuplicatePersonalDocument::class)
    fun handleDuplicatePersonalDocument(ex: DuplicatePersonalDocument): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = "duplicate_document"))
}