package br.com.deltaglobalbank.domain.account

import br.com.deltaglobalbank.internal_treasury.domain.account.Cpf
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters.PaysmartAccountAdapter
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder
import org.apache.hc.core5.ssl.SSLContexts
import org.junit.jupiter.api.Assertions.assertNull
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import kotlin.test.Test

class PaysmartAccountAdapterIntegrationTest {

    private val props = java.util.Properties().apply {
        load(java.io.File("src/main/resources/application-secret.properties").inputStream())
    }
    private val restClient: RestClient
    private val apiKey = props.getProperty("APIKEY_PAYSMART")
    private val baseUrl = props.getProperty("API_BASE_URL")
    private val certPassword = props.getProperty("APIKEY_CERT_PASSWORD")
    private val certPem = props.getProperty("PAYSMART_CRT").replace("\\n", "\n")
    private val keyPem = props.getProperty("PAYSMART_CERT_KEY").replace("\\n", "\n")

    init {
        val certFactory = CertificateFactory.getInstance("X.509")
        val cert = certFactory.generateCertificate(certPem.byteInputStream())

        val keyBytes = Base64.getDecoder().decode(
            keyPem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "")
                .trim()
        )
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec)

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)
        keyStore.setKeyEntry("paysmart", privateKey, certPassword.toCharArray(), arrayOf(cert))

        val sslContext = SSLContexts.custom()
            .loadKeyMaterial(keyStore, certPassword.toCharArray())
            .build()

        val httpClient = HttpClients.custom()
            .setConnectionManager(
                PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(
                        SSLConnectionSocketFactoryBuilder.create()
                            .setSslContext(sslContext)
                            .build()
                    )
                    .build()
            )
            .build()

        val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
        restClient = RestClient.builder().requestFactory(requestFactory).build()
    }

    private val adapter = PaysmartAccountAdapter(
        restClient,
        baseUrl
    )

    @Test
    fun `deve retornar account quando cpf existir na paysmart`() {
        println("baseUrl: $baseUrl")
        println("apiKey: $apiKey")
        val cpfValido = "11144477735"
        val cpf = Cpf(cpfValido)
        val account = adapter.findByCpf(cpf)

        val result = kotlin.test.assertNotNull(account)
        println("accountId: ${result.accountId}, accountNumber: ${result.accountNumber}")
    }

    @Test
    fun `deve retornar null quando cpf nao existir na paysmart`() {
        val cpfInexistente = Cpf("52998224725")
        val account = adapter.findByCpf(cpfInexistente)

        assertNull(account)
    }
}
