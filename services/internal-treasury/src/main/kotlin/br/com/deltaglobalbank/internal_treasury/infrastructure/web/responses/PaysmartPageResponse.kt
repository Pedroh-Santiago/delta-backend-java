package br.com.deltaglobalbank.internal_treasury.infrastructure.web.responses

class PaysmartPageResponse<T> (
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val hasNextPage: Boolean
)