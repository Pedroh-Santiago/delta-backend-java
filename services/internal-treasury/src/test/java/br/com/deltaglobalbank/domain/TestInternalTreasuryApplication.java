package br.com.deltaglobalbank.domain;

import br.com.deltaglobalbank.domain.internal_treasury.TestcontainersConfiguration;
import br.com.deltaglobalbank.internal_treasury.InternalTreasuryApplication;
import org.springframework.boot.SpringApplication;

public class TestInternalTreasuryApplication {

    public static void main(String[] args) {
        SpringApplication.from(InternalTreasuryApplication::main)
            .with(TestcontainersConfiguration.class)
            .run(args);
    }
}
