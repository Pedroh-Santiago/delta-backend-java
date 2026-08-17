package br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendPolicyDocumentResponse(
    String status,
    String identificador,
    String nomeArquivo,
    String guidDocumento,
    String verificacao,
    String error,
    String message
) {
}
