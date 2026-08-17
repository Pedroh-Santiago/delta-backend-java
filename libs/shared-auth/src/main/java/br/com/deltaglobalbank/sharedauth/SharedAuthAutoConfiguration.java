package br.com.deltaglobalbank.sharedauth;

import br.com.deltaglobalbank.sharedauth.revocation.RedisTokenRevocationChecker;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

@AutoConfiguration
@AutoConfigureAfter(name = "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration")
@EnableConfigurationProperties({JwtValidationProperties.class, RevocationProperties.class})
public class SharedAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtValidator jwtValidator(JwtValidationProperties properties) {
        return new JwtValidator(properties);
    }

    @Bean
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean(TokenRevocationChecker.class)
    public TokenRevocationChecker redisTokenRevocationChecker(StringRedisTemplate redis) {
        return new RedisTokenRevocationChecker(redis);
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
