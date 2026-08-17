package br.com.deltaglobalbank.internal_treasury.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TefRabbitMQConfig {

    public static final String TEF_QUEUE = "tef.approved";
    public static final String TEF_DLQ = "tef.approved.dlq";
    public static final String TEF_EXCHANGE = "tef.exchange";

    @Bean
    public Queue tefQueue() {
        return QueueBuilder.durable(TEF_QUEUE)
            .deadLetterExchange("")
            .deadLetterRoutingKey(TEF_DLQ)
            .build();
    }

    @Bean
    public Queue tefDlq() {
        return QueueBuilder.durable(TEF_DLQ).build();
    }

    @Bean
    public DirectExchange tefExchange() {
        return new DirectExchange(TEF_EXCHANGE);
    }

    @Bean
    public Binding tefBinding(Queue tefQueue, DirectExchange tefExchange) {
        return BindingBuilder.bind(tefQueue).to(tefExchange).with(TEF_QUEUE);
    }
}
