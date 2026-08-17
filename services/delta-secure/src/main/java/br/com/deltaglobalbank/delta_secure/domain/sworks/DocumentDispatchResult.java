package br.com.deltaglobalbank.delta_secure.domain.sworks;

public sealed interface DocumentDispatchResult permits DocumentDispatchResult.Success, DocumentDispatchResult.Failure {

    String idProposal();

    String identificador();

    String nomeArquivo();

    record Success(
        String idProposal,
        String identificador,
        String nomeArquivo,
        String guidDocumento,
        DocumentVerification verification
    ) implements DocumentDispatchResult {
    }

    record Failure(
        String idProposal,
        String identificador,
        String nomeArquivo,
        String error,
        String message
    ) implements DocumentDispatchResult {
    }
}
