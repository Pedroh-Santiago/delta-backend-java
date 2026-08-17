package br.com.deltaglobalbank.sharedauth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties({JwtValidationProperties.class, RevocationProperties.class})
public class SharedAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtValidator jwtValidator(JwtValidationProperties properties) {
        return new JwtValidator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
        JwtValidator jwtValidator,
        ObjectProvider<TokenRevocationChecker> revocationChecker,
        RevocationProperties revocationProperties
    ) {
        return new JwtAuthenticationFilter(
            jwtValidator,
            revocationChecker.getIfAvailable(),
            revocationProperties.revocationCheckOn(),
            revocationProperties.failOpenOnRedisError()
        );
    }
}
