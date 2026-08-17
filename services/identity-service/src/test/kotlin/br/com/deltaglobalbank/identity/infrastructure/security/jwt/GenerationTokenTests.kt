package br.com.deltaglobalbank.identity.infrastructure.security.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertAll
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerationTokenTests {
    private lateinit var keyManager: KeyManager

    private lateinit var jwtProperties: JwtIssuerProperties
    private lateinit var jwtIssuer: JwtIssuer
    private lateinit var publicKey: RSAPublicKey
    private lateinit var privateKey : RSAPrivateKey

    @BeforeEach
    fun setUp() {
        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        publicKey = keyPair.public as RSAPublicKey
        privateKey = keyPair.private as RSAPrivateKey

        keyManager = mockk()
        every { keyManager.getActiveSigningKey() } returns SigningKeyMaterial(
            kid = "test",
            publicKey = publicKey,
            privateKey = privateKey,
            status = "ACTIVE"
        )
        jwtProperties = mockk()
        every { jwtProperties.issuer } returns "delta"
        every { jwtProperties.audience } returns "delta-api"
        every { jwtProperties.accessTokenTtl } returns Duration.ofSeconds(900)

        jwtIssuer = JwtIssuer(keyManager, jwtProperties)
    }

    @Test
    fun `must write all user claims into the signed jwt`(){
        val userId = UUID.randomUUID()
        val tenantId = UUID.randomUUID()
        val claims = UserClaims(
            userId = userId,
            tenantId = tenantId,
            roles = listOf("customers.admin", "platform.admin"),
            modules = listOf("customers"),
            mustChangePassword = true
        )

        val issued = jwtIssuer.issueForUser(claims)

        val payload = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(issued.token)
            .payload

        assertAll(
            { assertEquals(userId.toString(), payload.subject) },
            { assertEquals(tenantId.toString(), payload["tenant_id"]) },
            { assertEquals("user", payload["principal_type"]) },
            { assertEquals(listOf("customers.admin", "platform.admin"), payload["roles"]) },
            { assertEquals(listOf("customers"), payload["modules"]) },
            { assertEquals(true, payload["must_change_password"]) },
            { assertEquals(issued.jti.toString(), payload.id) }
        )
    }

    @Test
    fun `must reject token verified with wrong public key`() {
        val claims = UserClaims(
            userId = UUID.randomUUID(),
            tenantId = UUID.randomUUID(),
            roles = listOf("customers.admin"),
            modules = listOf("customers"),
            mustChangePassword = false
        )

        val issued = jwtIssuer.issueForUser(claims)

        val wrongKeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val wrongPublicKey = wrongKeyPair.public as RSAPublicKey

        Assertions.assertThrows(SignatureException::class.java) {
            Jwts.parser()
                .verifyWith(wrongPublicKey)
                .build()
                .parseSignedClaims(issued.token)
        }
    }

    @Test
    fun `must set expiration according to access token ttl`() {
        val claims = UserClaims(
            userId = UUID.randomUUID(),
            tenantId = UUID.randomUUID(),
            roles = emptyList(),
            modules = emptyList(),
            mustChangePassword = false
        )

        val issued = jwtIssuer.issueForUser(claims)

        val payload = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(issued.token)
            .payload

        val issuedAt = payload.issuedAt.toInstant()
        val expiration = payload.expiration.toInstant()

        val ttlSeconds = Duration.between(issuedAt, expiration).seconds
        assertEquals(900, ttlSeconds)
    }

}