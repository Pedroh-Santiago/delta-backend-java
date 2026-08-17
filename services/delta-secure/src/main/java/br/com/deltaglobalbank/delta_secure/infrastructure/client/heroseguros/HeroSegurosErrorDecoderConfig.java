package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class HeroSegurosErrorDecoderConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new HeroSegurosErrorDecoder();
    }
}
