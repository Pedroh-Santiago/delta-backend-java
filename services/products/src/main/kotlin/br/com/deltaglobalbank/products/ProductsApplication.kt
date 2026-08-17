package br.com.deltaglobalbank.products

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ProductsApplication

fun main(args: Array<String>) {
	runApplication<ProductsApplication>(*args)
}
