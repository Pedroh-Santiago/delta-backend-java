package br.com.deltaglobalbank.delta_secure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros.ServiceAuthProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(
    classes = ApplicationSecretFileOverrideTest.TestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "DELTA_SECURE_SECRET_FILE=src/test/resources/fixtures/override-secret-test.properties"
)
class ApplicationSecretFileOverrideTest {

    @Configuration
    @EnableConfigurationProperties(ServiceAuthProperties.class)
    static class TestApp {
    }

    @Autowired
    private ServiceAuthProperties serviceAuthProperties;

    @Test
    void deltaSecureSecretFileRedirecionaOImportParaForaDeUserHome() {
        assertEquals("MARCADOR-OVERRIDE", serviceAuthProperties.convenios().get("clt").clientId());
    }
}
