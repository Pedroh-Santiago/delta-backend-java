package br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SWorksCreateProcessResponse(
    @JsonProperty("Identificador") String identificador,
    @JsonProperty("CodigoProcesso") Integer codigoProcesso
) {
    public SWorksCreateProcessResponse() {
        this(null, null);
    }
}
