package br.com.deltaglobalbank.delta_secure.features.heroseguros.getPolicy;

import java.util.List;

import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.Address;
import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.Coverage;

public record GetPolicyResponse(
    int id,
    int status,
    String createdAt,
    String updatedAt,
    String planName,
    GetPolicyCustomer customer,
    GetPolicyInfo info,
    List<Coverage> coverages
) {
}

record GetPolicyCustomer(
    String name,
    String docNumber,
    String email,
    String phone,
    String cellphone,
    Address address
) {
}

record GetPolicyInfo(
    double price,
    double iof,
    double debtAmount,
    int installments,
    String startDate,
    String endDate
) {
}
