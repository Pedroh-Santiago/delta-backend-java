package br.com.deltaglobalbank.delta_secure.features.heroseguros.getPolicy;

import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.Address;
import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.Coverage;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosGetPolicyResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosProposalAddress;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosProposalCoverage;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosProposalCustomer;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosProposalInfo;

public final class GetPolicyMappers {

    private GetPolicyMappers() {
    }

    public static Coverage toCoverage(HeroSegurosProposalCoverage coverage) {
        return new Coverage(
            coverage.id() != null ? coverage.id() : 0,
            coverage.coverageName() != null ? coverage.coverageName() : "",
            coverage.insuredAmount() != null ? coverage.insuredAmount() : "",
            coverage.waitingPeriodDays()
        );
    }

    public static Address toAddress(HeroSegurosProposalAddress address) {
        return new Address(
            address.cep() != null ? address.cep() : "",
            address.address() != null ? address.address() : "",
            address.number() != null ? address.number() : "",
            address.complement(),
            address.neighborhood() != null ? address.neighborhood() : "",
            address.city() != null ? address.city() : "",
            address.state() != null ? address.state() : ""
        );
    }

    public static GetPolicyCustomer toGetPolicyCustomer(HeroSegurosProposalCustomer customer) {
        return new GetPolicyCustomer(
            customer.name() != null ? customer.name() : "",
            customer.docNumber() != null ? customer.docNumber() : "",
            customer.email() != null ? customer.email() : "",
            customer.phone(),
            customer.cellphone(),
            customer.address() != null ? toAddress(customer.address()) : null
        );
    }

    public static GetPolicyInfo toGetPolicyInfo(HeroSegurosProposalInfo info) {
        return new GetPolicyInfo(
            info.price() != null ? info.price() : 0.0,
            info.iof() != null ? info.iof() : 0.0,
            info.debtAmount() != null ? info.debtAmount() : 0.0,
            info.installments() != null ? info.installments() : 0,
            info.startDate(),
            info.endDate()
        );
    }

    public static GetPolicyResponse toGetPolicyResponse(HeroSegurosGetPolicyResponse response) {
        var data = response.data();
        GetPolicyCustomer customer = data.customer() != null
            ? toGetPolicyCustomer(data.customer())
            : new GetPolicyCustomer("", "", "", null, null, null);
        GetPolicyInfo info = data.info() != null
            ? toGetPolicyInfo(data.info())
            : new GetPolicyInfo(0.0, 0.0, 0.0, 0, null, null);

        return new GetPolicyResponse(
            data.id() != null ? data.id() : 0,
            data.status() != null ? data.status() : 0,
            data.createdAt() != null ? data.createdAt() : "",
            data.updatedAt() != null ? data.updatedAt() : "",
            data.partnerPlan() != null && data.partnerPlan().name() != null ? data.partnerPlan().name() : "",
            customer,
            info,
            data.coverages().stream().map(GetPolicyMappers::toCoverage).toList()
        );
    }
}
