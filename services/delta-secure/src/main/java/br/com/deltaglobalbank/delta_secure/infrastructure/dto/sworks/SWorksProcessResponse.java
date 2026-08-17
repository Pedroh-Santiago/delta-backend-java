package br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SWorksProcessResponse(
    @JsonProperty("Identificador") String identificador,
    @JsonProperty("Codigo") Integer codigo,
    @JsonProperty("CodigoLegado") String codigoLegado,
    @JsonProperty("Status") Integer status,
    @JsonProperty("Documentos") List<SWorksProcessDocument> documentos
) {
    public SWorksProcessResponse {
        if (documentos == null) {
            documentos = List.of();
        }
    }

    public SWorksProcessResponse() {
        this(null, null, null, null, List.of());
    }
}
