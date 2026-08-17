package br.com.deltaglobalbank.products

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<ProductsApplication>().with(TestcontainersConfiguration::class).run(*args)
}
