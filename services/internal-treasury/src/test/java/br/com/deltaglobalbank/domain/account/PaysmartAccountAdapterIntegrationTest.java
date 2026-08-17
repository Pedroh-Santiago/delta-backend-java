package br.com.deltaglobalbank.domain.account;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import br.com.deltaglobalbank.internal_treasury.domain.account.Account;
import br.com.deltaglobalbank.internal_treasury.domain.account.Cpf;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters.PaysmartAccountAdapter;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class PaysmartAccountAdapterIntegrationTest {

    private final Properties props = new Properties();
    private final RestClient restClient;
    private final String apiKey;
    private final String baseUrl;
    private final String certPassword;
    private final String certPem;
    private final String keyPem;
    private final PaysmartAccountAdapter adapter;

    PaysmartAccountAdapterIntegrationTest() throws Exception {
        try (FileInputStream in = new FileInputStream("src/main/resources/application-secret.properties")) {
            props.load(in);
        }
        apiKey = props.getProperty("APIKEY_PAYSMART");
        baseUrl = props.getProperty("API_BASE_URL");
        certPassword = props.getProperty("APIKEY_CERT_PASSWORD");
        certPem = props.getProperty("PAYSMART_CRT").replace("\\n", "\n");
        keyPem = props.getProperty("PAYSMART_CERT_KEY").replace("\\n", "\n");

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        Certificate cert = certFactory.generateCertificate(
            new java.io.ByteArrayInputStream(certPem.getBytes()));

        byte[] keyBytes = Base64.getDecoder().decode(
            keyPem.replace("-----BEGIN PRIVATE KEY-----", "")
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

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(
                SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .build()
            )
            .build();

        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restClient = RestClient.builder().requestFactory(requestFactory).build();

        adapter = new PaysmartAccountAdapter(restClient, baseUrl);
    }

    @Test
    void deveRetornarAccountQuandoCpfExistirNaPaysmart() {
        System.out.println("baseUrl: " + baseUrl);
        System.out.println("apiKey: " + apiKey);
        String cpfValido = "11144477735";
        Cpf cpf = new Cpf(cpfValido);
        Account account = adapter.findByCpf(cpf);

        assertNotNull(account);
        System.out.println("accountId: " + account.accountId() + ", accountNumber: " + account.accountNumber());
    }

    @Test
    void deveRetornarNullQuandoCpfNaoExistirNaPaysmart() {
        Cpf cpfInexistente = new Cpf("52998224725");
        Account account = adapter.findByCpf(cpfInexistente);

        assertNull(account);
    }
}
