package br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SWorksCreateProcessRequest(
    @JsonProperty("codigoWorkflow") int codigoWorkflow,
    @JsonProperty("dadosEntrada") List<SWorksInputField> dadosEntrada
) {
}
