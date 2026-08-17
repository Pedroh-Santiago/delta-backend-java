package br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SWorksInputField(
    @JsonProperty("nome") String nome,
    @JsonProperty("valor") String valor
) {
}
