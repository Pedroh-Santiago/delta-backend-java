package br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument;

public record SendPolicyDocumentCommand(
    String idProposal,
    String ticket,
    String identificadorProcesso,
    byte[] documentBytes,
    boolean iniciarProcesso
) {
    public SendPolicyDocumentCommand(String idProposal, String ticket, String identificadorProcesso, byte[] documentBytes) {
        this(idProposal, ticket, identificadorProcesso, documentBytes, false);
    }
}
