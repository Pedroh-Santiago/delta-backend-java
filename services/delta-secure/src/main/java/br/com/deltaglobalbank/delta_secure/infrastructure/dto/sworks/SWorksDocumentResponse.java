package br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SWorksDocumentResponse(
    @JsonProperty("Identificador") String identificador,
    @JsonProperty("Nome") String nome,
    @JsonProperty("NomeTipificacao") String nomeTipificacao,
    @JsonProperty("DataCriacao") String dataCriacao,
    @JsonProperty("Status") String status,
    @JsonProperty("NivelAnalise") Integer nivelAnalise,
    @JsonProperty("Base64") String base64
) {
    public SWorksDocumentResponse() {
        this(null, null, null, null, null, null, null);
    }
}
