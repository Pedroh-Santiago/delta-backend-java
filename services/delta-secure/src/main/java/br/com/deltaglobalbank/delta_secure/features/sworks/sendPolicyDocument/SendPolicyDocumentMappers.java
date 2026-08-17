package br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument;

import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchResult;

public final class SendPolicyDocumentMappers {

    private SendPolicyDocumentMappers() {
    }

    public static SendPolicyDocumentResponse toResponse(DocumentDispatchResult result) {
        if (result instanceof DocumentDispatchResult.Success success) {
            return new SendPolicyDocumentResponse(
                "SUCESSO",
                success.identificador(),
                success.nomeArquivo(),
                success.guidDocumento(),
                success.verification().getClass().getSimpleName(),
                null,
                null
            );
        }

        DocumentDispatchResult.Failure failure = (DocumentDispatchResult.Failure) result;
        return new SendPolicyDocumentResponse(
            "FALHA",
            failure.identificador(),
            failure.nomeArquivo(),
            null,
            null,
            failure.error(),
            failure.message()
        );
    }
}
