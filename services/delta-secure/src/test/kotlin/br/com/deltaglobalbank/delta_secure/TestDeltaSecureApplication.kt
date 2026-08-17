package br.com.deltaglobalbank.delta_secure

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<DeltaSecureApplication>().with(TestcontainersConfiguration::class).run(*args)
}
