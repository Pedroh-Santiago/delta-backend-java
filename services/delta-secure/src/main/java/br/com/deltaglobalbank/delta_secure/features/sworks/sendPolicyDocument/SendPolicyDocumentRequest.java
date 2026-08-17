package br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument;

import jakarta.validation.constraints.NotBlank;

public record SendPolicyDocumentRequest(
    @NotBlank String idProposal,
    @NotBlank String identificadorProcesso
) {
}
