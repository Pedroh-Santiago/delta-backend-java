package br.com.deltaglobalbank.delta_secure.features.heroseguros.searchPolicies;

import java.util.List;

public record SearchPoliciesResponse(
    int recordsTotal,
    int recordsFiltered,
    List<PolicySummary> policies
) {
}

record PolicySummary(
    Integer id,
    String ticket,
    String planName,
    String customerName,
    String customerDocNumber,
    double price,
    double debtAmount,
    int installments,
    int status,
    String createdAt
) {
}
