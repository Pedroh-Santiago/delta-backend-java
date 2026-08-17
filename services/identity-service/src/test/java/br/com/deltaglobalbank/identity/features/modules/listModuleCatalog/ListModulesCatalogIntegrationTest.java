package br.com.deltaglobalbank.identity.features.modules.listModuleCatalog;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.TestcontainersConfiguration;
import br.com.deltaglobalbank.sharedauth.AuthenticatedPrincipal;
import br.com.deltaglobalbank.sharedauth.JwtAuthenticationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class ListModulesCatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private JwtAuthenticationToken auth(String... roles) {
        return new JwtAuthenticationToken(new AuthenticatedPrincipal(
            UUID.randomUUID(), UUID.randomUUID(), "user", List.of(roles), List.of(), false, UUID.randomUUID()));
    }

    @Test
    void platformAdminListsModuleCatalogWithDefaultModules() throws Exception {
        mockMvc.perform(
            get("/admin/modules")
                .with(authentication(auth("platform.admin")))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(6))
            .andExpect(jsonPath("$.items[?(@.code == 'identity')].name").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'customers')].name").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'products')].name").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'lending')].name").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'card')].name").exists())
            .andExpect(jsonPath("$.items[?(@.code == 'documents')].name").exists())
            .andExpect(jsonPath("$.items[0].code").exists())
            .andExpect(jsonPath("$.items[0].name").exists());
    }

    @Test
    void identityAdminCannotListModuleCatalog() throws Exception {
        mockMvc.perform(
            get("/admin/modules")
                .with(authentication(auth("identity.admin")))
        ).andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/admin/modules"))
            .andExpect(status().isUnauthorized());
    }
}
