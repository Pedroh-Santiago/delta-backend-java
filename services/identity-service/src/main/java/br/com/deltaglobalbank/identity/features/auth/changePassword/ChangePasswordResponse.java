package br.com.deltaglobalbank.identity.features.auth.changePassword;

public record ChangePasswordResponse(
    boolean success,
    String message
) {
    public ChangePasswordResponse() {
        this(true, "Senha alterada com sucesso. Faça login novamente.");
    }
}
