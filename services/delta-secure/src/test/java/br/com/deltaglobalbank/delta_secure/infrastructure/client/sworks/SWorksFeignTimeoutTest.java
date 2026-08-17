package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;

@SpringBootTest(
    classes = SWorksFeignTimeoutTest.TestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "sworks.auth.username=usuario-hml",
        "sworks.auth.password=senha-hml",
        "feign.client.config.default.connect-timeout=300",
        "feign.client.config.default.read-timeout=300"
    }
)
class SWorksFeignTimeoutTest {

    @Configuration
    @EnableConfigurationProperties(SWorksProperties.class)
    @EnableFeignClients(clients = SWorksAuthClient.class)
    @ImportAutoConfiguration({
        JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        FeignAutoConfiguration.class
    })
    static class TestApp {
    }

    @Autowired
    private SWorksAuthClient authClient;

    @Test
    void readTimeoutConfiguradoCortaAChamadaBemAntesDoDefaultDe60s() {
        sworks.stubFor(
            post(urlEqualTo("/token")).willReturn(
                aResponse().withFixedDelay(3_000).withStatus(200).withBody("{}")
            )
        );

        long inicio = System.currentTimeMillis();
        assertThrows(Exception.class, () -> authClient.login(new LinkedMultiValueMap<>()));
        long duracaoMs = System.currentTimeMillis() - inicio;

        assertTrue(
            duracaoMs < 3_000,
            "com read-timeout de 300ms configurado, a chamada nao deveria esperar os 3s de delay do stub (levou " + duracaoMs + "ms)"
        );
    }

    private static final WireMockServer sworks = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        sworks.start();
    }

    @DynamicPropertySource
    static void sworksProperties(DynamicPropertyRegistry registry) {
        registry.add("sworks.base-url", () -> "http://localhost:" + sworks.port());
    }

    @AfterAll
    static void pararWireMock() {
        sworks.stop();
    }
}
