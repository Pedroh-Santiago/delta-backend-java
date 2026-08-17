package br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SWorksProcessDocument(
    @JsonProperty("Identificador") String identificador,
    @JsonProperty("Nome") String nome,
    @JsonProperty("Formulario") String formulario,
    @JsonProperty("DataCriacao") String dataCriacao,
    @JsonProperty("StatusAnalise") Integer statusAnalise
) {
    public SWorksProcessDocument() {
        this(null, null, null, null, null);
    }
}
