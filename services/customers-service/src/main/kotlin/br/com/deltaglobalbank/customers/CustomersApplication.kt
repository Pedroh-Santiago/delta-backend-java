package br.com.deltaglobalbank.customers

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class CustomersApplication

fun main(args: Array<String>) {
	runApplication<CustomersApplication>(*args)
}
