package br.com.deltaglobalbank.identity.features.apiClients.listClients

data class ListClientsResponse(
    val items : List<ItemsResponse>,
    val page : Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
