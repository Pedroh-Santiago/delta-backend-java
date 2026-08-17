package br.com.deltaglobalbank.sharedauth

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(JwtValidationProperties::class,  RevocationProperties::class)
class SharedAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun jwtValidator(properties: JwtValidationProperties): JwtValidator {
        return JwtValidator(properties)
    }

    @Bean
    @ConditionalOnMissingBean
    fun jwtAuthenticationFilter(jwtValidator: JwtValidator, revocationChecker: ObjectProvider<TokenRevocationChecker>, revocationProperties: RevocationProperties): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(jwtValidator, revocationChecker.getIfAvailable(), revocationProperties.revocationCheckOn, revocationProperties.failOpenOnRedisError
        )
    }
}
