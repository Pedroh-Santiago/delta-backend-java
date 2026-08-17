package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.deltaglobalbank.delta_secure.infrastructure.client.backoffice.BackOfficeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class IntegrationPropertiesBindingTest {

    @EnableConfigurationProperties({SWorksProperties.class, BackOfficeProperties.class})
    static class PropertiesConfig {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(PropertiesConfig.class);

    @Test
    void ligaAsCredenciaisDoSworksEDoBackofficeSeparadamente() {
        runner.withPropertyValues(
            "sworks.base-url=https://sworkshml-delta.simply.com.br/SWorks.WebApi",
            "sworks.auth.username=sworks-user",
            "sworks.auth.password=sworks-pwd",
            "sworks.auth.grant-type=password",
            "sworks.verify-after-upload=false",
            "backoffice.base-url=https://backoffice.example",
            "backoffice.auth.username=backoffice-user",
            "backoffice.auth.password=backoffice-pwd"
        ).run(context -> {
            SWorksProperties sworks = context.getBean(SWorksProperties.class);
            assertEquals("https://sworkshml-delta.simply.com.br/SWorks.WebApi", sworks.baseUrl());
            assertEquals("sworks-user", sworks.auth().username());
            assertEquals("sworks-pwd", sworks.auth().password());
            assertEquals("password", sworks.auth().grantType());
            assertEquals(false, sworks.verifyAfterUpload());

            BackOfficeProperties backOffice = context.getBean(BackOfficeProperties.class);
            assertEquals("https://backoffice.example", backOffice.baseUrl());
            assertEquals("backoffice-user", backOffice.auth().username());
            assertEquals("backoffice-pwd", backOffice.auth().password());
        });
    }

    @Test
    void codigoDeWorkflowVazioLigaEmNuloENaoEmZero() {
        runner.withPropertyValues(
            "sworks.base-url=https://host/SWorks.WebApi",
            "sworks.processo.codigo-workflow=",
            "sworks.processo.cd-produto=",
            "sworks.processo.tipo-operacao="
        ).run(context -> {
            SWorksProcessSettings processo = context.getBean(SWorksProperties.class).processo();
            assertNull(processo.codigoWorkflow(), "vazio tem que virar null, senão a criação liga sozinha");
            assertTrue(processo.cdProduto() == null || processo.cdProduto().isBlank());
            assertTrue(processo.tipoOperacao() == null || processo.tipoOperacao().isBlank());
        });
    }

    @Test
    void codigoDeWorkflowPreenchidoLigaComoInteiro() {
        runner.withPropertyValues(
            "sworks.base-url=https://host/SWorks.WebApi",
            "sworks.processo.codigo-workflow=8",
            "sworks.processo.cd-produto=19"
        ).run(context -> {
            SWorksProcessSettings processo = context.getBean(SWorksProperties.class).processo();
            assertEquals(8, processo.codigoWorkflow());
            assertEquals("19", processo.cdProduto());
        });
    }

    @Test
    void usaPasswordComoGrantTypeEHabilitaAVerificacaoPorPadrao() {
        runner.withPropertyValues("sworks.base-url=https://host/SWorks.WebApi").run(context -> {
            SWorksProperties sworks = context.getBean(SWorksProperties.class);
            assertEquals("password", sworks.auth().grantType());
            assertTrue(sworks.verifyAfterUpload());
        });
    }
}
