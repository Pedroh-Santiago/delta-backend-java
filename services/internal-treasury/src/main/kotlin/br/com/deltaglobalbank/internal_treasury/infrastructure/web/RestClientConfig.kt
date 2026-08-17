package br.com.deltaglobalbank.internal_treasury.infrastructure.web

import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder
import org.apache.hc.core5.ssl.SSLContexts
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

@Configuration
class RestClientConfig(
    @Value("\${PAYSMART_CRT}") private val certPem: String,
    @Value("\${PAYSMART_CERT_KEY}") private val keyPem: String,
    @Value("\${APIKEY_CERT_PASSWORD}") private val certPassword: String
) {
    @Bean
    fun restClient(): RestClient {
        val certFactory = CertificateFactory.getInstance("X.509")
        val certClean = certPem.replace("\\n", "\n")
        val cert = certFactory.generateCertificate(certClean.byteInputStream())

        val keyClean = keyPem.replace("\\n", "\n")
        val keyBytes = Base64.getDecoder().decode(
            keyClean.replace("-----BEGIN PRIVATE KEY-----", "")
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

        return RestClient.builder()
            .requestFactory(requestFactory)
            .build()
    }
}