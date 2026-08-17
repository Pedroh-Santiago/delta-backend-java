package br.com.deltaglobalbank.delta_secure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DeltaSecureApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeltaSecureApplication.class, args);
	}
}
