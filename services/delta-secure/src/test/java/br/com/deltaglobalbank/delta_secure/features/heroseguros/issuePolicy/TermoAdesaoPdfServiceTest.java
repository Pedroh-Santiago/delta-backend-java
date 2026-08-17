package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.HeroSegurosCoverage;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.getPolicy.HeroSegurosProposalData;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicy;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicyData;
import org.junit.jupiter.api.Test;

class TermoAdesaoPdfServiceTest {

    private final TermoAdesaoPdfService service = new TermoAdesaoPdfService();

    @Test
    void generateProduzUmPdfAPartirDosDadosDaEmissao() {
        byte[] bytes = service.generate(request(), policyData());

        assertPdf(bytes);
    }

    @Test
    void generateFromProposalProduzUmPdfAPartirDaRespostaDoGetPolicy() {
        byte[] bytes = service.generateFromProposal(
            new HeroSegurosProposalData(),
            "TCK-1",
            "148030"
        );

        assertPdf(bytes);
    }

    @Test
    void generateFromProposalNaoQuebraQuandoCustomerEnderecoEInfoVemNulos() {
        byte[] bytes = service.generateFromProposal(
            new HeroSegurosProposalData(),
            "TCK-1",
            null
        );

        assertPdf(bytes);
    }

    private void assertPdf(byte[] bytes) {
        assertTrue(bytes.length > 0, "o pdf gerado nao deveria vir vazio");
        assertTrue("%PDF".equals(new String(Arrays.copyOfRange(bytes, 0, 4))), "o conteudo gerado deveria ser um pdf valido");
    }

    private IssuePolicyRequest request() {
        return new IssuePolicyRequest(
            Convenio.CLT,
            false,
            1000.0,
            12,
            "148030",
            null,
            null,
            new IssuePolicyCliente(
                "Cliente de Teste",
                "11144477735",
                "1990-01-01",
                1,
                "M",
                "11999999999",
                "cliente@example.com",
                new IssuePolicyEndereco(
                    "01310-100",
                    "Av Paulista",
                    "1000",
                    null,
                    "Bela Vista",
                    "São Paulo",
                    "SP"
                )
            ),
            null,
            null,
            null
        );
    }

    private HeroSegurosPolicyData policyData() {
        return new HeroSegurosPolicyData(
            7, "Plano", true, 12, "0", "100000", 18, 80, 12,
            "2026-01-01", "2027-01-01", 12.34, 100000, "1000.00", "1000.00",
            List.of(new HeroSegurosCoverage(1, "Morte", "1000", 30)),
            new HeroSegurosPolicy("TCK-1", "http://hero/policy")
        );
    }
}
