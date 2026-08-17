package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class SWorksErrorDecoderConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new SWorksErrorDecoder();
    }
}
