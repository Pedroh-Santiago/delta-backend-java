package br.com.deltaglobalbank.identity.infrastructure.web.responses;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
    String error,
    String message,
    Instant lockedUntil
) {
    public ApiErrorResponse(String error) {
        this(error, null, null);
    }

    public ApiErrorResponse(String error, String message) {
        this(error, message, null);
    }
}
