package br.com.deltaglobalbank.customers.infrastructure.web;

import br.com.deltaglobalbank.customers.domain.customer.BankAccountNotFound;
import br.com.deltaglobalbank.customers.domain.customer.CpfAlreadyExists;
import br.com.deltaglobalbank.customers.domain.customer.CustomerNotFound;
import br.com.deltaglobalbank.customers.domain.customer.DuplicateBankAccount;
import br.com.deltaglobalbank.customers.domain.customer.DuplicatePersonalDocument;
import br.com.deltaglobalbank.customers.domain.customer.DuplicatePrimaryAccountForPurpose;
import br.com.deltaglobalbank.customers.domain.customer.InvalidStatusFilter;
import br.com.deltaglobalbank.customers.domain.customer.StatusUnchanged;
import br.com.deltaglobalbank.customers.domain.customer.SubaggregateDoesNotBelongToCustomer;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.deltaglobalbank.customers.infrastructure.web.responses.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.error("Erro não tratado", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiErrorResponse("internal_error"));
    }

    @ExceptionHandler(CustomerNotFound.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerNotFounf(CustomerNotFound ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse("customer_not_found"));
    }

    @ExceptionHandler(BankAccountNotFound.class)
    public ResponseEntity<ApiErrorResponse> handleBankAccountNotFound(BankAccountNotFound ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse("bank_account_not_found"));
    }

    @ExceptionHandler(CpfAlreadyExists.class)
    public ResponseEntity<ApiErrorResponse> handleCpfAlreadyExists(CpfAlreadyExists ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ApiErrorResponse("cpf_already_exists"));
    }

    @ExceptionHandler(DuplicatePrimaryAccountForPurpose.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicatePrimaryAccountForPurpose(DuplicatePrimaryAccountForPurpose ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse("multiple_primary_per_purpose"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse(ex.getMessage() != null ? ex.getMessage() : "invalid_argument"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse("invalid_request", describeFieldErrors(ex)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse("invalid_request", ex.getMessage()));
    }

    private String describeFieldErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return fieldErrors.toString();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse("malformed_request"));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleDenied(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ApiErrorResponse("denied"));
    }

    @ExceptionHandler(SubaggregateDoesNotBelongToCustomer.class)
    public ResponseEntity<ApiErrorResponse> handleSubaggregate(SubaggregateDoesNotBelongToCustomer ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse("subaggregate_does_not_belong_to_customer"));
    }

    @ExceptionHandler(DuplicateBankAccount.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateAccount(DuplicateBankAccount ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse("duplicate_bank_account"));
    }

    @ExceptionHandler(DuplicatePersonalDocument.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicatePersonalDocument(DuplicatePersonalDocument ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse("duplicate_document"));
    }

    @ExceptionHandler(StatusUnchanged.class)
    public ResponseEntity<ApiErrorResponse> handleStatusUnchanged(StatusUnchanged ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiErrorResponse("status_unchanged"));
    }

    @ExceptionHandler(InvalidStatusFilter.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidStatusFilter(InvalidStatusFilter ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse("invalid_status_filter"));
    }
}
