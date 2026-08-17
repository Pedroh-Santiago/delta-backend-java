package br.com.deltaglobalbank.identity.features.modules.listModuleCatalog

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
class ListModulesCatalogIntegrationTest {

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
    fun `platform admin lists module catalog with default modules`() {
        mockMvc.perform(
            get("/admin/modules")
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(6))
            .andExpect(jsonPath("$.items[?(@.code == 'identity')].name").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'customers')].name").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'products')].name").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'lending')].name").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'card')].name").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'documents')].name").exists())
            .andExpect(jsonPath("$.items[0].code").exists())
            .andExpect(jsonPath("$.items[0].name").exists())
    }

    @Test
    fun `identity admin cannot list module catalog`() {
        mockMvc.perform(
            get("/admin/modules")
                .with(authentication(auth("identity.admin")))
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `unauthenticated request is rejected`() {
        mockMvc.perform(get("/admin/modules"))
            .andExpect(status().isUnauthorized)
    }
}
