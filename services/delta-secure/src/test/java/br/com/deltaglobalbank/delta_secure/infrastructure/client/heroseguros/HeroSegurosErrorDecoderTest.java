package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosRequestRejected;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosUnavailable;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

class HeroSegurosErrorDecoderTest {

    private static final String DEFAULT_METHOD_KEY = "HeroSegurosQuotationClient#quote(String,HeroSegurosQuotationRequest)";

    private final HeroSegurosErrorDecoder decoder = new HeroSegurosErrorDecoder();

    @Test
    void cincoXXViraIndisponivel() {
        assertInstanceOf(HeroSegurosUnavailable.class, decode(500, null, DEFAULT_METHOD_KEY));
        assertInstanceOf(HeroSegurosUnavailable.class, decode(503, null, DEFAULT_METHOD_KEY));
    }

    @Test
    void quatroXXViraRequisicaoRecusadaPreservandoOCorpoDoErro() {
        HeroSegurosRequestRejected erro = assertInstanceOf(
            HeroSegurosRequestRejected.class,
            decode(400, "{\"error\":\"debt_amount invalido\"}", DEFAULT_METHOD_KEY)
        );

        assertTrue(erro.getMessage().contains("debt_amount invalido"));
    }

    @Test
    void quatrocentosEVinteDoisTambemViraRequisicaoRecusada() {
        assertInstanceOf(HeroSegurosRequestRejected.class, decode(422, "entidade inválida", DEFAULT_METHOD_KEY));
    }

    @Test
    void mensagemCitaOStatusEOMethodKeyDaChamada() {
        HeroSegurosRequestRejected erro = assertInstanceOf(
            HeroSegurosRequestRejected.class,
            decode(400, "corpo", "HeroSegurosQuotationClient#quote(String,HeroSegurosQuotationRequest)")
        );

        assertTrue(erro.getMessage().contains("400"));
        assertTrue(erro.getMessage().contains("HeroSegurosQuotationClient#quote"));
    }

    @Test
    void respostaSemCorpoNaoQuebraODecoder() {
        HeroSegurosRequestRejected erro = assertInstanceOf(
            HeroSegurosRequestRejected.class,
            decode(400, null, DEFAULT_METHOD_KEY)
        );

        assertTrue(erro.getMessage().contains("sem corpo"));
    }

    private Exception decode(int status, String body, String methodKey) {
        Request request = Request.create(
            Request.HttpMethod.POST,
            "https://api.homologacao.heroseguros.com.br/api/prestamista/quotation",
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        Response.Builder builder = Response.builder()
            .status(status)
            .reason("erro")
            .request(request)
            .headers(Map.of());
        if (body != null) {
            builder.body(body, StandardCharsets.UTF_8);
        }
        return decoder.decode(methodKey, builder.build());
    }
}
