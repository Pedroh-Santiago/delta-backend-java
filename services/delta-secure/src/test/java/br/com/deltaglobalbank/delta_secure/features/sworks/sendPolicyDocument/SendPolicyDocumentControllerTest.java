package br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchResult;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentVerification;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy.TermoAdesaoRegenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class SendPolicyDocumentControllerTest {

    private final SendPolicyDocumentToSWorksUseCase useCase = mock(SendPolicyDocumentToSWorksUseCase.class);
    private final TermoAdesaoRegenerationService regenerationService = mock(TermoAdesaoRegenerationService.class);

    private final MockMvc mockMvc = buildMockMvc();

    private MockMvc buildMockMvc() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders
            .standaloneSetup(new SendPolicyDocumentController(useCase, regenerationService))
            .setValidator(validator)
            .build();
    }

    private final String rota = "/heroseguros/prestamista/policies/TCK-4477/termo-adesao/sworks";
    private final String corpo = "{\"idProposal\":\"PROP-9911\",\"identificadorProcesso\":\"proc-1\"}";

    @BeforeEach
    void setUp() {
        when(regenerationService.regenerate(any())).thenReturn("pdf-bytes".getBytes());
    }

    @Test
    void sucessoDevolve200ComOGuidDoDocumento() throws Exception {
        when(useCase.execute(any())).thenReturn(sucesso());

        mockMvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCESSO"))
            .andExpect(jsonPath("$.guidDocumento").value("guid-1"))
            .andExpect(jsonPath("$.verificacao").value("Confirmed"));
    }

    @Test
    void oTicketVemDaRotaEORestoDoCorpo() throws Exception {
        ArgumentCaptor<SendPolicyDocumentCommand> comando = ArgumentCaptor.forClass(SendPolicyDocumentCommand.class);
        when(useCase.execute(comando.capture())).thenReturn(sucesso());

        mockMvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo));

        assertEquals("TCK-4477", comando.getValue().ticket());
        assertEquals("PROP-9911", comando.getValue().idProposal());
        assertEquals("proc-1", comando.getValue().identificadorProcesso());
    }

    @Test
    void sworksIndisponivelDevolve503() throws Exception {
        when(useCase.execute(any())).thenReturn(falha("SWorksUnavailable"));

        mockMvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.status").value("FALHA"))
            .andExpect(jsonPath("$.error").value("SWorksUnavailable"));
    }

    @Test
    void processoInexistenteDevolve404() throws Exception {
        when(useCase.execute(any())).thenReturn(falha("SWorksProcessNotFound"));

        mockMvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isNotFound());
    }

    @Test
    void documentoRecusadoDevolve422() throws Exception {
        when(useCase.execute(any())).thenReturn(falha("SWorksDocumentRejected"));

        mockMvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void documentoGravadoDivergenteDevolve502() throws Exception {
        when(useCase.execute(any())).thenReturn(falha("SWorksDocumentMismatch"));

        mockMvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isBadGateway());
    }

    @Test
    void falhaDeCredencialOuPermissaoDevolve502ENao4xx() throws Exception {
        for (String erro : new String[] {
            "SWorksAuthenticationFailed", "SWorksAccessDenied", "SWorksNotConfigured", "SWorksTokenExpired"
        }) {
            when(useCase.execute(any())).thenReturn(falha(erro));

            mockMvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadGateway());
        }
    }

    @Test
    void erroInesperadoDevolve500() throws Exception {
        when(useCase.execute(any())).thenReturn(falha("IOException"));

        mockMvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void idProposalEmBrancoERecusadoAntesDeChamarOUseCase() throws Exception {
        mockMvc.perform(
            post(rota).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idProposal\":\"  \",\"identificadorProcesso\":\"proc-1\"}")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void identificadorDoProcessoEmBrancoERecusado() throws Exception {
        mockMvc.perform(
            post(rota).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idProposal\":\"PROP-9911\",\"identificadorProcesso\":\"\"}")
        ).andExpect(status().isBadRequest());
    }

    private DocumentDispatchResult.Success sucesso() {
        return new DocumentDispatchResult.Success(
            "PROP-9911", "proc-1", "termo-adesao-TCK-4477.pdf", "guid-1", new DocumentVerification.Confirmed("guid-1")
        );
    }

    private DocumentDispatchResult.Failure falha(String erro) {
        return new DocumentDispatchResult.Failure(
            "PROP-9911", "proc-1", "termo-adesao-TCK-4477.pdf", erro, "detalhe do erro"
        );
    }
}
