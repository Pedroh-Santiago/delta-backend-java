package br.com.deltaglobalbank.customers.infrastructure.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
    String error,
    String message
) {
    public ApiErrorResponse(String error) {
        this(error, null);
    }
}
