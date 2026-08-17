package br.com.deltaglobalbank.customers.infrastructure.web.responses

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiErrorResponse(
    val error: String,
    val message: String? = null
)