package br.com.deltaglobalbank.identity

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<IdentityApplication>().with(TestcontainersConfiguration::class).run(*args)
}
