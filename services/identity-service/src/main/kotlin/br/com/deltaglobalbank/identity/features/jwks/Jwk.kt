package br.com.deltaglobalbank.identity.features.jwks

data class JwkResponse(
    val keys: List<Jwk>
)

data class Jwk(
    val kty: String,
    val use: String,
    val alg: String,
    val kid: String,
    val n: String,
    val e: String
)