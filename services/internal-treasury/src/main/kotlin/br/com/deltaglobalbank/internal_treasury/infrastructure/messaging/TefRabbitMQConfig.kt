package br.com.deltaglobalbank.internal_treasury.infrastructure.messaging

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TefRabbitMQConfig {

    companion object {
        const val TEF_QUEUE = "tef.approved"
        const val TEF_DLQ = "tef.approved.dlq"
        const val TEF_EXCHANGE = "tef.exchange"
    }

    @Bean
    fun tefQueue(): Queue {
        return QueueBuilder.durable(TEF_QUEUE)
            .deadLetterExchange("")
            .deadLetterRoutingKey(TEF_DLQ)
            .build()
    }

    @Bean
    fun tefDlq(): Queue {
        return QueueBuilder.durable(TEF_DLQ).build()
    }

    @Bean
    fun tefExchange(): DirectExchange {
        return DirectExchange(TEF_EXCHANGE)
    }

    @Bean
    fun tefBinding(tefQueue: Queue, tefExchange: DirectExchange): Binding {
        return BindingBuilder.bind(tefQueue).to(tefExchange).with(TEF_QUEUE)
    }
}