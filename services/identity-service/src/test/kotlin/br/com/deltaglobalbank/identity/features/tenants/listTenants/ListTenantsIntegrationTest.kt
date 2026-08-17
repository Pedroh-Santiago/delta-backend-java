package br.com.deltaglobalbank.identity.features.tenants.listTenants

import br.com.deltaglobalbank.identity.TestcontainersConfiguration
import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.TenantEntity
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaTenantRepository
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@Transactional
class ListTenantsIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jpaTenantRepository: JpaTenantRepository

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

    private fun seedTenant(name: String, slug: String, status: String = "active"): UUID {
        val id = UUID.randomUUID()
        jpaTenantRepository.save(
            TenantEntity(
                id = id,
                name = name,
                slug = slug,
                status = status,
                createdAt = Instant.now()
            )
        )
        return id
    }

    @Test
    fun `platform admin lists tenants`() {
        seedTenant("Acme Corp", "acme")
        seedTenant("Beta Inc", "beta", status = "suspended")

        mockMvc.perform(
            get("/admin/tenants")
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].name").exists())
            .andExpect(jsonPath("$.items[0].slug").exists())
            .andExpect(jsonPath("$.items[0].status").exists())
    }

    @Test
    fun `identity admin cannot list tenants`() {
        mockMvc.perform(
            get("/admin/tenants")
                .with(authentication(auth("identity.admin")))
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `pagination works`() {
        seedTenant("Tenant A", "tenant-a")
        seedTenant("Tenant B", "tenant-b")

        mockMvc.perform(
            get("/admin/tenants?page=0&size=1")
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.items.length()").value(1))
    }

    @Test
    fun `soft deleted tenant is not listed`() {
        val id = seedTenant("Deleted Corp", "deleted-corp")
        seedTenant("Active Corp", "active-corp")

        jpaTenantRepository.deleteById(id)

        mockMvc.perform(
            get("/admin/tenants")
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.items[0].slug").value("active-corp"))
    }
}
