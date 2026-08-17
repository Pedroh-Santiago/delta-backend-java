package br.com.deltaglobalbank.internal_treasury

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InternalTreasuryApplication

fun main(args: Array<String>) {
	runApplication<InternalTreasuryApplication>(*args)
}
