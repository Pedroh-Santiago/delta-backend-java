package br.com.deltaglobalbank.delta_secure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.ConvenioCredentials;
import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.ServiceAuthProperties;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(
    classes = ApplicationSecretPropertiesLoadTest.TestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class ApplicationSecretPropertiesLoadTest {

    @Configuration
    @EnableConfigurationProperties(ServiceAuthProperties.class)
    static class TestApp {
    }

    @Autowired
    private ServiceAuthProperties serviceAuthProperties;

    @Test
    void carregaOs3ConveniosDoArquivoMovidoParaForaDoClasspath() {
        assertEquals(3, serviceAuthProperties.convenios().size(), "convenios: " + serviceAuthProperties.convenios().keySet());
        assertTrue(serviceAuthProperties.convenios().containsKey("clt"));
        assertTrue(serviceAuthProperties.convenios().containsKey("siape"));
        assertTrue(serviceAuthProperties.convenios().containsKey("prefeituras"));
    }

    @Test
    void credenciaisDos3ConveniosNaoVemVazias() {
        for (Map.Entry<String, ConvenioCredentials> entry : serviceAuthProperties.convenios().entrySet()) {
            String convenio = entry.getKey();
            ConvenioCredentials credenciais = entry.getValue();
            assertTrue(!credenciais.clientId().isBlank(), convenio + ".clientId vazio");
            assertTrue(!credenciais.clientSecret().isBlank(), convenio + ".clientSecret vazio");
            assertTrue(!credenciais.username().isBlank(), convenio + ".username vazio");
            assertTrue(!credenciais.password().isBlank(), convenio + ".password vazio");
        }
    }
}
