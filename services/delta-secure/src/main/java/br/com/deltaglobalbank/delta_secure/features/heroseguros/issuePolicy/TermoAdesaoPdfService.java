package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.BrazilianDate;
import br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects.MonetaryAmount;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosProposalAddress;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosProposalCustomer;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosProposalData;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosAddress;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicyData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class TermoAdesaoPdfService {

    public byte[] generate(IssuePolicyRequest request, HeroSegurosPolicyData policyData) {
        return fill(buildFieldValues(toFieldSource(request, policyData)));
    }

    public byte[] generateFromProposal(HeroSegurosProposalData proposal, String ticket, String externalId) {
        return fill(buildFieldValues(toFieldSource(proposal, ticket, externalId)));
    }

    private byte[] fill(Map<String, String> fieldValues) {
        byte[] templateBytes;
        try (var inputStream = new ClassPathResource("termo_adesao_prestamista.pdf").getInputStream()) {
            templateBytes = inputStream.readAllBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return PdfFieldOverlayFiller.fill(templateBytes, fieldValues);
    }

    private TermoAdesaoFieldSource toFieldSource(IssuePolicyRequest request, HeroSegurosPolicyData policyData) {
        IssuePolicyEndereco address = request.customer().address();
        return new TermoAdesaoFieldSource(
            request.customer().name(),
            request.customer().docNumber(),
            request.customer().birthday(),
            address.address(),
            address.number(),
            address.complement(),
            address.neighborhood(),
            address.cep(),
            address.city(),
            address.state(),
            request.externalId(),
            request.customer().phone(),
            request.customer().email(),
            policyData.startDate(),
            policyData.endDate(),
            policyData.policy().ticket(),
            MonetaryAmount.parseBrl(policyData.price()),
            policyData.iof()
        );
    }

    private TermoAdesaoFieldSource toFieldSource(HeroSegurosProposalData proposal, String ticket, String externalId) {
        HeroSegurosProposalCustomer customer = proposal.customer();
        HeroSegurosProposalAddress address = customer != null ? customer.address() : null;

        String celular;
        if (customer != null && customer.cellphone() != null) {
            celular = customer.cellphone();
        } else if (customer != null && customer.phone() != null) {
            celular = customer.phone();
        } else {
            celular = "";
        }

        return new TermoAdesaoFieldSource(
            customer != null && customer.name() != null ? customer.name() : "",
            customer != null && customer.docNumber() != null ? customer.docNumber() : "",
            customer != null ? customer.birthday() : null,
            address != null && address.address() != null ? address.address() : "",
            address != null && address.number() != null ? address.number() : "",
            address != null ? address.complement() : null,
            address != null && address.neighborhood() != null ? address.neighborhood() : "",
            address != null && address.cep() != null ? address.cep() : "",
            address != null && address.city() != null ? address.city() : "",
            address != null && address.state() != null ? address.state() : "",
            externalId,
            celular,
            customer != null && customer.email() != null ? customer.email() : "",
            proposal.info() != null ? proposal.info().startDate() : null,
            proposal.info() != null ? proposal.info().endDate() : null,
            ticket,
            proposal.info() != null ? proposal.info().price() : null,
            proposal.info() != null ? proposal.info().iof() : null
        );
    }

    private Map<String, String> buildFieldValues(TermoAdesaoFieldSource source) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("SEGURADO", source.nome());
        values.put("CPF/CNPJ", source.cpfCnpj());
        values.put("DATA DE NASCIMENTO", formatDate(source.nascimento()));
        values.put("ENDEREÇO", source.endereco());
        values.put("NÚMERO", source.numero());
        values.put("COMPLEMENTO", source.complemento() != null ? source.complemento() : "");
        values.put("BAIRRO", source.bairro());
        values.put("CEP", source.cep());
        values.put("CIDADE", source.cidade());
        values.put("UF", source.uf());
        values.put("N° CONTRATO", source.contrato() != null ? source.contrato() : "");
        values.put("DDD / CELULAR", source.celular());
        values.put("E-MAIL", source.email());
        values.put("INÍCIO DE VIGÊNCIA", formatDate(source.inicioVigencia()));
        values.put("FIM DE VIGÊNCIA", formatDate(source.fimVigencia()));
        values.put("N° APÓLICE", source.apolice());
        values.putAll(buildPagamentoFieldValues(source));
        return values;
    }

    private Map<String, String> buildPagamentoFieldValues(TermoAdesaoFieldSource source) {
        Double bruto = source.precoBruto();
        if (bruto == null) {
            return Map.of();
        }
        double iof = source.iof() != null ? source.iof() : 0.0;
        double liquido = bruto - iof;

        Map<String, String> values = new LinkedHashMap<>();
        values.put("PRÊMIO LÍQUIDO TOTAL (R$)", MonetaryAmount.toDisplayString(liquido));
        values.put("IOF (R$)", MonetaryAmount.toDisplayString(iof));
        values.put("PRÊMIO BRUTO TOTAL (R$)", MonetaryAmount.toDisplayString(bruto));
        return values;
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return "";
        }
        return BrazilianDate.toBrDisplayOrRaw(rawDate);
    }

    private record TermoAdesaoFieldSource(
        String nome,
        String cpfCnpj,
        String nascimento,
        String endereco,
        String numero,
        String complemento,
        String bairro,
        String cep,
        String cidade,
        String uf,
        String contrato,
        String celular,
        String email,
        String inicioVigencia,
        String fimVigencia,
        String apolice,
        Double precoBruto,
        Double iof
    ) {
    }
}
