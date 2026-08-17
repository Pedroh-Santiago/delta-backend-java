package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.com.deltaglobalbank.delta_secure.domain.policy.Convenio;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReference;
import br.com.deltaglobalbank.delta_secure.domain.policy.TermoAdesaoReferenceRepository;
import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchResult;
import br.com.deltaglobalbank.delta_secure.features.sworks.DocumentDispatchNotifier;
import br.com.deltaglobalbank.delta_secure.features.sworks.LoggingDocumentDispatchNotifier;
import br.com.deltaglobalbank.delta_secure.features.sworks.sendPolicyDocument.SendPolicyDocumentToSWorksUseCase;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.ClockConfig;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.Sleeper;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.ThreadSleeper;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosAuthClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosIssuePolicyClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.HeroSegurosQuotationClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.ServiceAuthProperties;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.TokenService;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.DocumentBase64Encoder;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksAuthClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksBearerExecutor;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksDocumentClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksDocumentSender;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksDocumentVerifier;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksProcessClient;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksProcessCreator;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksProperties;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks.SWorksTokenService;
import br.com.deltaglobalbank.delta_secure.infrastructure.dto.sworks.SWorksInputField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = EmissaoDiagnosticoProbeTest.ProbeApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "EMISSAO_DIAGNOSTICO", matches = "true")
class EmissaoDiagnosticoProbeTest {

    @Configuration
    @EnableConfigurationProperties({
        SWorksProperties.class,
        ServiceAuthProperties.class,
        PrestamistaPolicyProperties.class
    })
    @EnableFeignClients(clients = {
        SWorksAuthClient.class, SWorksDocumentClient.class, SWorksProcessClient.class,
        HeroSegurosAuthClient.class, HeroSegurosQuotationClient.class, HeroSegurosIssuePolicyClient.class
    })
    @ImportAutoConfiguration({
        JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        FeignAutoConfiguration.class
    })
    @Import({
        ClockConfig.class, ThreadSleeper.class,
        SWorksTokenService.class, SWorksBearerExecutor.class, DocumentBase64Encoder.class,
        SWorksDocumentVerifier.class, SWorksDocumentSender.class, SWorksProcessCreator.class,
        TokenService.class, TermoAdesaoPdfService.class,
        LoggingDocumentDispatchNotifier.class, SendPolicyDocumentToSWorksUseCase.class,
        IssuePolicyUseCase.class
    })
    static class ProbeApp {
        @Bean
        TermoAdesaoReferenceRepository termoAdesaoReferenceRepository() {
            return new TermoAdesaoReferenceRepository() {
                private final Map<String, TermoAdesaoReference> store = new ConcurrentHashMap<>();

                @Override
                public TermoAdesaoReference save(TermoAdesaoReference reference) {
                    store.put(reference.ticket(), reference);
                    return reference;
                }

                @Override
                public TermoAdesaoReference findByTicket(String ticket) {
                    return store.get(ticket);
                }
            };
        }
    }

    @Autowired
    private IssuePolicyUseCase useCase;

    @Autowired
    private SWorksProperties sworksProperties;

    @Autowired
    private SWorksProcessCreator processCreator;

    @Autowired
    private PrestamistaPolicyProperties prestamista;

    @Autowired
    private Sleeper sleeper;

    @Autowired
    private DocumentDispatchNotifier notifier;

    @Test
    void emiteERelataOndeOEnvioAoSworksPara() {
        StringBuilder relatorio = new StringBuilder("DIAGNOSTICO DA EMISSAO + ENVIO AO SWORKS\n\n");

        relatorio.append("[0] Configuracao lida pela aplicacao").append('\n');
        relatorio.append("    sworks.base-url            = ").append(sworksProperties.baseUrl()).append('\n');
        relatorio.append("    codigo-workflow            = ")
            .append(sworksProperties.processo().codigoWorkflow() != null ? sworksProperties.processo().codigoWorkflow() : "(vazio)")
            .append('\n');
        String cdProduto = sworksProperties.processo().cdProduto();
        relatorio.append("    cd-produto                 = ")
            .append(cdProduto != null && !cdProduto.isBlank() ? cdProduto : "(vazio)")
            .append('\n');
        String tipoOperacao = sworksProperties.processo().tipoOperacao();
        relatorio.append("    tipo-operacao              = ")
            .append(tipoOperacao != null && !tipoOperacao.isBlank() ? tipoOperacao : "(vazio)")
            .append('\n');
        relatorio.append("    verify-after-upload        = ").append(sworksProperties.verifyAfterUpload()).append('\n');
        relatorio.append("    criacao habilitada?        = ").append(processCreator.habilitado()).append('\n');
        relatorio.append("    type-of-product configurado= ").append(prestamista.typeOfProduct()).append('\n');
        relatorio.append('\n');

        if (!processCreator.habilitado()) {
            relatorio.append(">>> CAUSA ENCONTRADA: criacao de processo DESLIGADA.").append('\n');
            relatorio.append("    A aplicacao nao esta lendo sworks.processo.codigo-workflow.").append('\n');
            gravar(relatorio);
            return;
        }

        relatorio.append("[1] Emitindo apolice na HeroSeguros e enviando ao SWorks...").append('\n');
        IssuePolicyResponse resultado;
        try {
            resultado = useCase.execute(requisicao());
        } catch (Exception ex) {
            relatorio.append("    A EMISSAO FALHOU antes de chegar ao SWorks:").append('\n');
            relatorio.append("    ").append(ex.getClass().getSimpleName()).append(": ").append(ex.getMessage()).append('\n');
            relatorio.append('\n');
            relatorio.append(">>> CAUSA ENCONTRADA: a apolice nao foi emitida, entao nao existe PDF").append('\n');
            relatorio.append("    para enviar. O SWorks nunca e chamado. Corrigir a emissao primeiro.").append('\n');
            gravar(relatorio);
            return;
        }

        relatorio.append("    ticket            = ").append(resultado.ticket()).append('\n');
        relatorio.append("    termoAdesaoUrl    = ")
            .append(resultado.termoAdesaoUrl() != null ? resultado.termoAdesaoUrl() : "(nulo - PDF nao foi gerado)")
            .append('\n');
        relatorio.append("    sworksStatus      = ")
            .append(resultado.sworksStatus() != null ? resultado.sworksStatus() : "(nulo - envio nao aconteceu)")
            .append('\n');
        relatorio.append("    sworksGuidDocumento = ")
            .append(resultado.sworksGuidDocumento() != null ? resultado.sworksGuidDocumento() : "-")
            .append('\n');
        relatorio.append("    sworksIdentificadorProcesso = ")
            .append(resultado.sworksIdentificadorProcesso() != null ? resultado.sworksIdentificadorProcesso() : "-")
            .append('\n');
        relatorio.append('\n');

        if (resultado.termoAdesaoUrl() == null) {
            relatorio.append(">>> CAUSA: o PDF do termo nao foi gerado; sem arquivo nao ha envio.").append('\n');
        } else if (resultado.sworksStatus() == null) {
            relatorio.append(">>> O envio nao aconteceu. Repetindo a criacao DIRETO no creator").append('\n');
            relatorio.append("    para a excecao aparecer (o use case engole e so loga).").append('\n');
            relatorio.append('\n');

            IssuePolicyRequest requisicao = requisicao();
            List<SWorksInputField> campos = SWorksProcessFieldsMapper.toSWorksInputFields(
                requisicao, resultado.ticket(), processCreator.settings(), requisicao.externalId()
            );
            relatorio.append("    dadosEntrada que serao enviados (").append(campos.size()).append(" campos):").append('\n');
            for (SWorksInputField campo : campos) {
                relatorio.append("      ").append(campo.nome()).append(" = ").append(campo.valor()).append('\n');
            }
            relatorio.append('\n');

            try {
                var criado = processCreator.create(campos);
                relatorio.append("    CRIOU AGORA: identificador=").append(criado.identificador())
                    .append(" codigo=").append(criado.codigoProcesso()).append('\n');
                relatorio.append("    >>> Se criou aqui e nao criou no fluxo, o problema esta ANTES da criacao.").append('\n');
            } catch (Exception ex) {
                relatorio.append("    >>> EXCECAO NA CRIACAO:").append('\n');
                relatorio.append("        ").append(ex.getClass().getName()).append('\n');
                relatorio.append("        ").append(ex.getMessage()).append('\n');
                Throwable causa = ex.getCause();
                int nivel = 1;
                while (causa != null && nivel <= 3) {
                    relatorio.append("        causa ").append(nivel).append(": ")
                        .append(causa.getClass().getSimpleName()).append(": ").append(causa.getMessage()).append('\n');
                    causa = causa.getCause();
                    nivel++;
                }
            }
        } else if ("SUCESSO".equals(resultado.sworksStatus())) {
            relatorio.append(">>> FUNCIONOU. O documento esta no SWorks e foi conferido.").append('\n');
        } else {
            relatorio.append(">>> O envio foi tentado e FALHOU. Ver log para error/message.").append('\n');
        }

        gravar(relatorio);
    }

    private IssuePolicyRequest requisicao() {
        String convenio = System.getenv("EMISSAO_CONVENIO");
        String externalId = System.getenv("EMISSAO_EXTERNAL_ID");
        return new IssuePolicyRequest(
            Convenio.valueOf(convenio != null ? convenio : "PREFEITURAS"),
            true,
            5000.00,
            20,
            externalId != null ? externalId : "148010",
            "2027-07-20",
            null,
            new IssuePolicyCliente(
                "Cliente Diagnostico BACK148",
                "11144477735",
                "1990-01-01",
                1,
                "M",
                "(11) 93398-9960",
                "cliente.diagnostico@example.com",
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
            4800.00,
            250.00
        );
    }

    private void gravar(StringBuilder relatorio) {
        File destino = new File("build/emissao-diagnostico.txt");
        File parentFile = destino.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        try {
            Files.writeString(destino.toPath(), relatorio.toString());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @SuppressWarnings("unused")
    private DocumentDispatchResult descarta(DocumentDispatchResult r) {
        return r;
    }
}
