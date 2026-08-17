package br.com.deltaglobalbank.domain.internal_treasury

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class InternalTreasuryApplicationTests {

	@Test
	fun contextLoads() {
	}

}
