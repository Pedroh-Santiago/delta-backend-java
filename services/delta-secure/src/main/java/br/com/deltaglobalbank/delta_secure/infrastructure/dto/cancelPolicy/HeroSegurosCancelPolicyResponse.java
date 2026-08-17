package br.com.deltaglobalbank.delta_secure.infrastructure.dto.cancelPolicy;

public record HeroSegurosCancelPolicyResponse(
    boolean success,
    String notifications
) {
}
