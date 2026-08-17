package br.com.deltaglobalbank.identity.features.modules.listModuleCatalog

data class ListModulesResponse(
    val items: List<ListedModule>
)

data class ListedModule(
    val code: String,
    val name: String,
    val description: String?
)
