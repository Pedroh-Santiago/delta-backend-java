package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.deltaglobalbank.delta_secure.infrastructure.dto.HeroSegurosCustomer;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosAddress;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicyRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.issuePolicy.HeroSegurosPolicyResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationCustomer;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationPlan;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.quotation.HeroSegurosQuotationResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies.HeroSegurosSearchPoliciesRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.searchPolicies.HeroSegurosSearchPoliciesResponse;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.token.HeroSegurosTokenRequest;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.token.HeroSegurosTokenResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
    classes = HeroSegurosWireTest.TestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class HeroSegurosWireTest {

    @Configuration
    @EnableFeignClients(clients = {
        HeroSegurosAuthClient.class,
        HeroSegurosQuotationClient.class,
        HeroSegurosIssuePolicyClient.class,
        HeroSegurosCancelPolicyClient.class,
        HeroSegurosGetPolicyClient.class,
        HeroSegurosSearchPoliciesClient.class
    })
    @ImportAutoConfiguration({
        JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        FeignAutoConfiguration.class
    })
    static class TestApp {
    }

    @Autowired
    private HeroSegurosAuthClient authClient;

    @Autowired
    private HeroSegurosQuotationClient quotationClient;

    @Autowired
    private HeroSegurosIssuePolicyClient issuePolicyClient;

    @Autowired
    private HeroSegurosCancelPolicyClient cancelPolicyClient;

    @Autowired
    private HeroSegurosGetPolicyClient getPolicyClient;

    @Autowired
    private HeroSegurosSearchPoliciesClient searchPoliciesClient;

    private final String authorization = "Bearer token-teste";

    @BeforeEach
    void limpar() {
        hero.resetAll();
    }

    @Test
    void loginVaiComoJsonComOsCamposEmSnakeCase() {
        hero.stubFor(post(urlEqualTo("/oauth/token")).willReturn(okJson(tokenJson())));

        HeroSegurosTokenResponse resposta = authClient.getToken(
            new HeroSegurosTokenRequest("password", "353", "segredo", "usuario-hml", "senha-hml", "api")
        );

        assertEquals("token-abc", resposta.accessToken());
        assertEquals("bearer", resposta.tokenType());
        assertEquals(3600L, resposta.expiresIn());

        hero.verify(
            postRequestedFor(urlEqualTo("/oauth/token"))
                .withHeader("Content-Type", matching("application/json(;.*)?"))
        );
        String corpo = hero.findAll(postRequestedFor(urlEqualTo("/oauth/token"))).get(0).getBodyAsString();
        assertContainsAll(corpo, "\"grant_type\":\"password\"", "\"client_id\":\"353\"", "\"client_secret\":\"segredo\"");
    }

    @Test
    void quotationEnviaDebtAmountETypeOfProductEmSnakeCaseEDesserializaAsCoberturas() {
        hero.stubFor(post(urlEqualTo("/api/prestamista/quotation")).willReturn(okJson(quotationResponseJson())));

        HeroSegurosQuotationResponse resposta = quotationClient.quote(
            authorization,
            new HeroSegurosQuotationRequest(10_000.0, 24, 9, new HeroSegurosQuotationCustomer(30))
        );

        assertEquals(1, resposta.data().size());
        HeroSegurosQuotationPlan plano = resposta.data().get(0);
        assertEquals("Plano A", plano.name());
        assertEquals(1, plano.coverages().size());
        assertEquals(30, plano.coverages().get(0).waitingPeriodDays());

        String corpo = hero.findAll(postRequestedFor(urlEqualTo("/api/prestamista/quotation"))).get(0).getBodyAsString();
        assertContainsAll(corpo, "\"debt_amount\":10000.0", "\"type_of_product\":9", "\"age\":30");
    }

    @Test
    void issuePolicyEnviaOClienteEEnderecoAninhadosEDesserializaHasPolicyPeriodComoBooleano() {
        hero.stubFor(post(urlEqualTo("/api/prestamista/proposal")).willReturn(okJson(issuePolicyResponseJson())));

        HeroSegurosPolicyResponse resposta = issuePolicyClient.issuePolicy(authorization, issuePolicyRequest());

        assertEquals("TCK-1", resposta.data().policy().ticket());
        assertTrue(resposta.data().hasPolicyPeriod(), "has_policy_period vem true (booleano) na resposta real da Hero, nao 1");
        assertEquals(148030, resposta.data().id());
        assertEquals(1, resposta.data().coverages().size());

        String corpo = hero.findAll(postRequestedFor(urlEqualTo("/api/prestamista/proposal"))).get(0).getBodyAsString();
        assertContainsAll(
            corpo,
            "\"partner_plan_id\":7", "\"external_id\":\"148030\"", "\"doc_number\":\"11144477735\"",
            "\"neighborhood\":\"Centro\""
        );
    }

    @Test
    void cancelPolicyEnviaMultipartComDocNumberTicketEReason() {
        hero.stubFor(post(urlEqualTo("/api/prestamista/proposal/cancel")).willReturn(okJson("{\"success\":true,\"notifications\":\"cancelado\"}")));

        var resposta = cancelPolicyClient.cancel(authorization, "11144477735", "TCK-1", "pedido do cliente");

        assertTrue(resposta.success());
        assertEquals("cancelado", resposta.notifications());

        String corpo = hero.findAll(postRequestedFor(urlEqualTo("/api/prestamista/proposal/cancel"))).get(0).getBodyAsString();
        assertContainsAll(corpo, "11144477735", "TCK-1", "pedido do cliente");
    }

    @Test
    void getPolicyUsaOIdNaRotaEDesserializaHasPolicyPeriodComoBooleanoNoPartnerPlan() {
        hero.stubFor(get(urlEqualTo("/api/prestamista/proposal/148030")).willReturn(okJson(getPolicyResponseJson())));

        var resposta = getPolicyClient.getProposal(authorization, 148030);

        assertEquals("Cliente Teste", resposta.data().customer().name());
        assertEquals(true, resposta.data().partnerPlan().hasPolicyPeriod());

        hero.verify(getRequestedFor(urlEqualTo("/api/prestamista/proposal/148030")).withHeader("Authorization", equalTo(authorization)));
    }

    @Test
    void searchPoliciesEnviaCreatedAtEmSnakeCaseEDesserializaHasPolicyPeriodComoBooleanoNosResultados() {
        hero.stubFor(post(urlEqualTo("/api/prestamista/proposals")).willReturn(okJson(searchPoliciesResponseJson())));

        HeroSegurosSearchPoliciesResponse resposta = searchPoliciesClient.search(
            authorization,
            new HeroSegurosSearchPoliciesRequest("2026-01-01", "2026-01-31", null)
        );

        assertEquals(1, resposta.data().data().size());
        assertEquals(true, resposta.data().data().get(0).plan().hasPolicyPeriod());
        assertNull(resposta.data().data().get(0).info().days(), "campo opcional ausente no fixture deve ficar nulo, nao quebrar o parse");

        String corpo = hero.findAll(postRequestedFor(urlEqualTo("/api/prestamista/proposals"))).get(0).getBodyAsString();
        assertContainsAll(corpo, "\"created_at\":\"2026-01-01\"", "\"created_at_end\":\"2026-01-31\"");
        assertFalse(corpo.contains("\"operation\":\"\""), "operation nulo nao deveria virar string vazia no corpo");
    }

    private HeroSegurosPolicyRequest issuePolicyRequest() {
        return new HeroSegurosPolicyRequest(
            true,
            7,
            9,
            "148030",
            10_000.0,
            10_500.0,
            24,
            "2028-08-06",
            new HeroSegurosCustomer(
                "Cliente Teste",
                "11144477735",
                "1990-01-01",
                1,
                "M",
                "11999998888",
                "cliente@example.com",
                new HeroSegurosAddress(
                    "01000-000",
                    "Rua Teste",
                    "100",
                    null,
                    "Centro",
                    "São Paulo",
                    "SP"
                )
            )
        );
    }

    private String tokenJson() {
        return "{\"token_type\":\"bearer\",\"expires_in\":3600,\"access_token\":\"token-abc\",\"refresh_token\":\"refresh-abc\"}";
    }

    private String quotationResponseJson() {
        return """
            {"success":true,"data":[{"partner_plan_id":7,"product_name":"Prestamista","name":"Plano A",
            "min_debt":"0","max_debt":"100000","min_age":18,"max_age":70,"installments":24,"price":"500.00",
            "coverages":[{"id":1,"coverage_name":"Morte","is":"10000.00","waiting_period_days":30}]}]}
            """;
    }

    private String issuePolicyResponseJson() {
        return """
            {"success":true,"data":{"partner_plan_id":7,"name":"Plano Prestamista","has_policy_period":true,
            "policy_period_months":12,"min_debt":"0","max_debt":"100000","min_age":18,"max_age":70,
            "installments":24,"start_date":"2026-08-06","end_date":"2027-08-06","iof":123.45,
            "price_cents":500000,"price":"5000.00","debt_amount":"10000.00",
            "coverages":[{"id":1,"coverage_name":"Morte","is":"10000.00","waiting_period_days":30}],
            "policy":{"ticket":"TCK-1","url":"https://hero/policy/1"},"id":148030},"notifications":[]}
            """;
    }

    private String getPolicyResponseJson() {
        return """
            {"success":true,"data":{"id":148030,"partner_plan_id":7,"customer_id":1,"guarantor_id":null,
            "policy_purchase_info_id":1,"status":5,"created_at":"2026-08-06T10:00:00","updated_at":"2026-08-06T10:00:00",
            "info":{"id":1,"price_cents":500000,"price":5000.0,"iof":123.45,"debt_amount":10000.0,"installments":24,
            "installments_amount":"416.67","type_of_charge_id":1,"start_date":"2026-08-06","end_date":"2027-08-06","days":365},
            "coverages":[{"id":1,"coverage_name":"Morte","is":"10000.00","waiting_period_days":30}],
            "partner_plan":{"partner_plan_id":7,"name":"Plano Prestamista","has_policy_period":true,
            "policy_period_months":12,"loan_term":null,"loan_term_months":null},
            "customer":{"id":1,"customer_address_id":1,"customer_civil_id":1,"customer_gender_id":1,
            "name":"Cliente Teste","doc_type":"cpf","doc_number":"11144477735","birthday":"1990-01-01",
            "phone":"11999998888","cellphone":null,"email":"cliente@example.com",
            "gender":{"id":1,"name":"Masculino","code":"M"},"civil":{"id":1,"name":"Solteiro"},
            "address":{"id":1,"cep":"01000-000","address":"Rua Teste","number":"100","complement":null,
            "neighborhood":"Centro","city":"São Paulo","state":"SP"}},"guarantor":null},"notifications":[]}
            """;
    }

    private String searchPoliciesResponseJson() {
        return """
            {"success":true,"data":{"draw":1,"recordsTotal":1,"recordsFiltered":1,"data":[
            {"id":148030,"ticket":"TCK-1","plan":{"partner_plan_id":7,"name":"Plano Prestamista",
            "has_policy_period":true,"policy_period_months":12,"loan_term":null,"loan_term_months":null},
            "customer":{"id":1,"name":"Cliente Teste","doc_number":"11144477735","email":"cliente@example.com"},
            "info":{"id":1,"price_cents":500000,"price":5000.0,"iof":123.45,"debt_amount":10000.0,"installments":24,
            "installments_amount":"416.67","type_of_charge_id":1,"start_date":"2026-08-06","end_date":"2027-08-06"},
            "status":5,"created_at":"2026-08-06T10:00:00","updated_at":"2026-08-06T10:00:00"}]},"notifications":null}
            """;
    }

    private void assertContainsAll(String corpo, String... trechos) {
        for (String trecho : trechos) {
            assertTrue(corpo.contains(trecho), "esperava encontrar '" + trecho + "' no corpo: " + corpo);
        }
    }

    private static final WireMockServer hero = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        hero.start();
    }

    @DynamicPropertySource
    static void heroProperties(DynamicPropertyRegistry registry) {
        registry.add("heroseguros.base-url", () -> "http://localhost:" + hero.port());
    }

    @AfterAll
    static void pararWireMock() {
        hero.stop();
    }
}
