package br.com.deltaglobalbank.internal_treasury.infrastructure.web;

import br.com.deltaglobalbank.internal_treasury.domain.account.AccountNotFoundException;
import br.com.deltaglobalbank.internal_treasury.domain.internalTransference.InsufficientBalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiErrorResponse(
        String error,
        String message
    ) {
        public ApiErrorResponse(String error) {
            this(error, null);
        }
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ApiErrorResponse("insufficient_balance"));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse("account_not_found"));
    }
}
