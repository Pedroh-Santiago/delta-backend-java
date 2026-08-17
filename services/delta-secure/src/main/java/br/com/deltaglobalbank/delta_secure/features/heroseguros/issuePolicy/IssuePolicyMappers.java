package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import java.time.LocalDate;
import java.time.Period;

import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.Coverage;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.HeroSegurosCoverage;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.HeroSegurosCustomer;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosAddress;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicyRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicyResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationCustomer;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationRequest;

public final class IssuePolicyMappers {

    private IssuePolicyMappers() {
    }

    public static HeroSegurosQuotationRequest toHeroSegurosQuotationRequest(IssuePolicyRequest request, int typeOfProduct) {
        int age = Period.between(LocalDate.parse(request.customer().birthday()), LocalDate.now()).getYears();
        return new HeroSegurosQuotationRequest(
            request.debtAmount(),
            request.installments(),
            typeOfProduct,
            new HeroSegurosQuotationCustomer(age)
        );
    }

    public static HeroSegurosAddress toHeroSegurosAddress(IssuePolicyEndereco endereco) {
        return new HeroSegurosAddress(
            endereco.cep(),
            endereco.address(),
            endereco.number(),
            endereco.complement(),
            endereco.neighborhood(),
            endereco.city(),
            endereco.state()
        );
    }

    public static HeroSegurosCustomer toHeroSegurosCustomer(IssuePolicyCliente cliente) {
        return new HeroSegurosCustomer(
            cliente.name(),
            cliente.docNumber(),
            cliente.birthday(),
            cliente.civil(),
            cliente.gender(),
            cliente.phone(),
            cliente.email(),
            toHeroSegurosAddress(cliente.address())
        );
    }

    public static HeroSegurosPolicyRequest toHeroSegurosPolicyRequest(IssuePolicyRequest request, int partnerPlanId, int typeOfProduct) {
        return new HeroSegurosPolicyRequest(
            request.billed(),
            partnerPlanId,
            typeOfProduct,
            request.externalId(),
            request.debtAmount(),
            request.chargedAmount(),
            request.installments(),
            request.lastInstallmentDate(),
            toHeroSegurosCustomer(request.customer())
        );
    }

    public static Coverage toCoverage(HeroSegurosCoverage coverage) {
        return new Coverage(
            coverage.id(),
            coverage.coverageName(),
            coverage.insuredAmount(),
            coverage.waitingPeriodDays()
        );
    }

    public static IssuePolicyResponse toIssuePolicyResponse(HeroSegurosPolicyResponse response) {
        return new IssuePolicyResponse(
            response.data().policy().ticket(),
            response.data().policy().url(),
            response.data().price(),
            response.data().coverages().stream().map(IssuePolicyMappers::toCoverage).toList()
        );
    }
}
