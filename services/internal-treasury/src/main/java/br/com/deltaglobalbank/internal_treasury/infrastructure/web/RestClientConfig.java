package br.com.deltaglobalbank.internal_treasury.infrastructure.web;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RestClientConfig {

    private final String certPem;
    private final String keyPem;
    private final String certPassword;

    public RestClientConfig(
        @Value("${PAYSMART_CRT}") String certPem,
        @Value("${PAYSMART_CERT_KEY}") String keyPem,
        @Value("${APIKEY_CERT_PASSWORD}") String certPassword
    ) {
        this.certPem = certPem;
        this.keyPem = keyPem;
        this.certPassword = certPassword;
    }

    @Bean
    public RestClient restClient() throws Exception {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        String certClean = certPem.replace("\\n", "\n");
        Certificate cert = certFactory.generateCertificate(
            new ByteArrayInputStream(certClean.getBytes(StandardCharsets.UTF_8))
        );

        String keyClean = keyPem.replace("\\n", "\n");
        byte[] keyBytes = Base64.getDecoder().decode(
            keyClean.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "")
                .trim()
        );
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("paysmart", privateKey, certPassword.toCharArray(), new Certificate[]{cert});

        SSLContext sslContext = SSLContexts.custom()
            .loadKeyMaterial(keyStore, certPassword.toCharArray())
            .build();

        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(
                PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(
                        SSLConnectionSocketFactoryBuilder.create()
                            .setSslContext(sslContext)
                            .build()
                    )
                    .build()
            )
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
            new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
            .requestFactory(requestFactory)
            .build();
    }
}
