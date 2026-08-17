package br.com.deltaglobalbank.customers

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<CustomersApplication>().with(TestcontainersConfiguration::class).run(*args)
}
