package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import java.util.ArrayList;
import java.util.List;

import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.BrazilianDate;
import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.Cpf;
import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.Digits;
import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.MonetaryAmount;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksProcessSettings;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksInputField;

public final class SWorksProcessFieldsMapper {

    private SWorksProcessFieldsMapper() {
    }

    public static List<SWorksInputField> toSWorksInputFields(
        IssuePolicyRequest request,
        String ticket,
        SWorksProcessSettings settings,
        String proposta
    ) {
        List<SWorksInputField> fields = new ArrayList<>();
        fields.add(new SWorksInputField("Nome", request.customer().name()));
        fields.add(new SWorksInputField("cpf", new Cpf(request.customer().docNumber()).masked()));
        fields.add(new SWorksInputField("email", request.customer().email()));
        fields.add(new SWorksInputField("celular", Digits.onlyDigits(request.customer().phone())));
        fields.add(new SWorksInputField("dtNascimento", BrazilianDate.toBrDisplayOrRaw(request.customer().birthday())));
        fields.add(new SWorksInputField("vlTotal", MonetaryAmount.toSWorksString(request.debtAmount())));
        fields.add(new SWorksInputField("prazo", String.valueOf(request.installments())));

        if (proposta != null) {
            fields.add(new SWorksInputField("Proposta", proposta));
        }

        if (request.installmentAmount() != null) {
            fields.add(new SWorksInputField("vlrParcela", MonetaryAmount.toSWorksString(request.installmentAmount())));
        }
        if (request.netAmount() != null) {
            fields.add(new SWorksInputField("vlrLiquido", MonetaryAmount.toSWorksString(request.netAmount())));
        }
        if (settings.cdProduto() != null && !settings.cdProduto().isBlank()) {
            fields.add(new SWorksInputField("cdProdut", settings.cdProduto()));
        }
        if (settings.tipoOperacao() != null && !settings.tipoOperacao().isBlank()) {
            fields.add(new SWorksInputField("tipoOperacao", settings.tipoOperacao()));
        }

        return fields;
    }
}
