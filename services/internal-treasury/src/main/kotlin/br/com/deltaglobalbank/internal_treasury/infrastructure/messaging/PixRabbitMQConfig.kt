package br.com.deltaglobalbank.internal_treasury.infrastructure.messaging

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PixRabbitMQConfig {

    companion object{
        const val PIX_QUEUE = "pix.approved"
        const val PIX_DLQ = "pix.approved.dlq"
        const val PIX_EXCHANGE = "pix.approved.exchange"
    }

    @Bean
    fun pixQueue(): Queue {
        return QueueBuilder.durable(PIX_QUEUE)
            .deadLetterExchange("")
            .deadLetterRoutingKey(PIX_DLQ)
            .build()
    }

    @Bean
    fun pixDql(): Queue {
        return QueueBuilder.durable(PIX_DLQ).build()
    }

    @Bean
    fun pixExchange(): DirectExchange {
        return DirectExchange(PIX_EXCHANGE)
    }

    @Bean
    fun pixBinding(pixQueue: Queue, pixExchange: DirectExchange): Binding {
        return BindingBuilder.bind(pixQueue).to(pixExchange).with(PIX_QUEUE)
    }
}