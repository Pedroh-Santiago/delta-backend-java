package br.com.deltaglobalbank.domain.internal_treasury

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.rabbitmq.RabbitMQContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.containers.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	fun postgresContainer(): PostgreSQLContainer<*> =
		PostgreSQLContainer(DockerImageName.parse("postgres:15"))

	@Bean
	@ServiceConnection
	fun rabbitContainer(): RabbitMQContainer =
		RabbitMQContainer(DockerImageName.parse("rabbitmq:latest"))

}
