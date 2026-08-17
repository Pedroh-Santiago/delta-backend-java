package br.com.deltaglobalbank.identity.features.roles.listRoles

import br.com.deltaglobalbank.identity.TestcontainersConfiguration
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
@Transactional
class ListRolesIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    private fun auth(vararg roles: String) = JwtAuthenticationToken(
        AuthenticatedPrincipal(
            subject = UUID.randomUUID(),
            tenantId = UUID.randomUUID(),
            principalType = "user",
            roles = roles.toList(),
            modules = emptyList(),
            mustChangePassword = false,
            jti = UUID.randomUUID()
        )
    )

    @Test
    fun `platform admin lists assignable roles including platform admin`() {
        mockMvc.perform(
            get("/admin/roles")
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(org.hamcrest.Matchers.greaterThan(0)))
            .andExpect(jsonPath("$.items[?(@.code == 'platform.admin')]").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'identity.admin')].name").exists())
            .andExpect(jsonPath("$.items[0].code").exists())
            .andExpect(jsonPath("$.items[0].name").exists())
    }

    @Test
    fun `identity admin lists assignable roles without platform admin`() {
        mockMvc.perform(
            get("/admin/roles")
                .with(authentication(auth("identity.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(org.hamcrest.Matchers.greaterThan(0)))
            .andExpect(jsonPath("$.items[?(@.code == 'platform.admin')]").doesNotExist())
            .andExpect(jsonPath("$.items[?(@.code == 'identity.admin')]").exists())
    }

    @Test
    fun `unauthorized role cannot list roles`() {
        mockMvc.perform(
            get("/admin/roles")
                .with(authentication(auth("customers.viewer")))
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `unauthenticated request is rejected`() {
        mockMvc.perform(get("/admin/roles"))
            .andExpect(status().isUnauthorized)
    }
}
