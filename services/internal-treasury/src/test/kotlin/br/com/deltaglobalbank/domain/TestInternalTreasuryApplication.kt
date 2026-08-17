package br.com.deltaglobalbank.domain

import br.com.deltaglobalbank.domain.internal_treasury.TestcontainersConfiguration
import br.com.deltaglobalbank.internal_treasury.InternalTreasuryApplication
import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<InternalTreasuryApplication>().with(TestcontainersConfiguration::class).run(*args)
}
