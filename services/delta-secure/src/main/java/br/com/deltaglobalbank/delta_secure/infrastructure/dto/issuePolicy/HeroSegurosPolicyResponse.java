package br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy;

import java.util.List;

public record HeroSegurosPolicyResponse(
    boolean success,
    HeroSegurosPolicyData data,
    List<String> notifications
) {
}
