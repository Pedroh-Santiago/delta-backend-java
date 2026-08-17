package br.com.deltaglobalbank.products.infrastructure.security

import br.com.deltaglobalbank.products.TestcontainersConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
class SecurityConfigTests(
    @Autowired val mockMvc: MockMvc
) {

    @Test
    fun `health e publico`() {
        mockMvc.get("/actuator/health").andExpect { status { isOk() } }
    }

    @Test
    fun `rota protegida sem token retorna 401`() {
        mockMvc.get("/qualquer-rota").andExpect { status { isUnauthorized() } }
    }

}