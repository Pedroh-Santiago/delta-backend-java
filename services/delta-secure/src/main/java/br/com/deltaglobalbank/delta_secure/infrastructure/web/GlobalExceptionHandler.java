package br.com.deltaglobalbank.delta_secure.infrastructure.web;

import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosInvalidResponse;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosNoPlanAvailable;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosRequestRejected;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosUnavailable;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.TermoAdesaoNaoEncontrado;
import br.com.deltaglobalbank.delta_secure.infrastructure.web.responses.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse(ex.getMessage() != null ? ex.getMessage() : "invalid_argument"));
    }

    @ExceptionHandler(HeroSegurosUnavailable.class)
    public ResponseEntity<ApiErrorResponse> handleHeroSegurosUnavailable(HeroSegurosUnavailable ex) {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ApiErrorResponse("heroseguros_unavailable"));
    }

    @ExceptionHandler(HeroSegurosRequestRejected.class)
    public ResponseEntity<ApiErrorResponse> handleHeroSegurosRequestRejected(HeroSegurosRequestRejected ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(new ApiErrorResponse("heroseguros_request_rejected", ex.getMessage()));
    }

    @ExceptionHandler(HeroSegurosInvalidResponse.class)
    public ResponseEntity<ApiErrorResponse> handleHeroSegurosInvalidResponse(HeroSegurosInvalidResponse ex) {
        log.error("Resposta inesperada da Hero Seguros", ex);
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(new ApiErrorResponse("heroseguros_invalid_response"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.error("Erro não tratado", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiErrorResponse("internal_error"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("");
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse("validation_error", message));
    }

    @ExceptionHandler(HeroSegurosNoPlanAvailable.class)
    public ResponseEntity<ApiErrorResponse> handleHeroSegurosNoPlanAvailable(HeroSegurosNoPlanAvailable ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse("heroseguros_no_plan_available"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse("malformed_request"));
    }

    @ExceptionHandler(TermoAdesaoNaoEncontrado.class)
    public ResponseEntity<ApiErrorResponse> handleTermoAdesaoNaoEncontrado(TermoAdesaoNaoEncontrado ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse("termo_adesao_nao_encontrado"));
    }
}
