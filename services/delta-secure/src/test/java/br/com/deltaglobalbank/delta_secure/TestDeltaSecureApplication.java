package br.com.deltaglobalbank.delta_secure;

import org.springframework.boot.SpringApplication;

public class TestDeltaSecureApplication {

	public static void main(String[] args) {
		SpringApplication.from(DeltaSecureApplication::main)
			.with(TestcontainersConfiguration.class)
			.run(args);
	}
}
