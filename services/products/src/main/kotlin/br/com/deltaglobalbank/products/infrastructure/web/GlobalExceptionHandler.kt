package br.com.deltaglobalbank.products.infrastructure.web

import br.com.deltaglobalbank.products.domain.product.DuplicateActiveProduct
import br.com.deltaglobalbank.products.domain.product.ProductNotFound
import br.com.deltaglobalbank.products.infrastructure.web.responses.ApiErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
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

    @ExceptionHandler(DuplicateActiveProduct::class)
    fun handleDuplicateActiveProduct(ex: DuplicateActiveProduct) =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse("duplicate_active_product"))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(ex.message ?: "invalid_argument"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(ex.message ?: "invalid_argument"))

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleDenied(ex: AuthorizationDeniedException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponse(error = "denied"))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(error = "malformed_request"))

    @ExceptionHandler(ProductNotFound::class)
    fun handleProductNotFound(ex: ProductNotFound): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(error = "product_not_found"))
}