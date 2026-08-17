package br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SWorksAttachDocumentRequest(
    @JsonProperty("NomeArquivo") String nomeArquivo,
    @JsonProperty("BytesBase64") String bytesBase64,
    @JsonProperty("FormatoOriginal") boolean formatoOriginal,
    @JsonProperty("Formulario") String formulario,
    @JsonProperty("IdentificadorObjeto") String identificadorObjeto
) {
    public SWorksAttachDocumentRequest(String nomeArquivo, String bytesBase64) {
        this(nomeArquivo, bytesBase64, true, null, null);
    }

    public SWorksAttachDocumentRequest(String nomeArquivo, String bytesBase64, String formulario) {
        this(nomeArquivo, bytesBase64, true, formulario, null);
    }
}
