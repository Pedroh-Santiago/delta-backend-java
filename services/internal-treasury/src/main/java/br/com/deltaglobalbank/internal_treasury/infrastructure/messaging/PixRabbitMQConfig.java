package br.com.deltaglobalbank.internal_treasury.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PixRabbitMQConfig {

    public static final String PIX_QUEUE = "pix.approved";
    public static final String PIX_DLQ = "pix.approved.dlq";
    public static final String PIX_EXCHANGE = "pix.approved.exchange";

    @Bean
    public Queue pixQueue() {
        return QueueBuilder.durable(PIX_QUEUE)
            .deadLetterExchange("")
            .deadLetterRoutingKey(PIX_DLQ)
            .build();
    }

    @Bean
    public Queue pixDql() {
        return QueueBuilder.durable(PIX_DLQ).build();
    }

    @Bean
    public DirectExchange pixExchange() {
        return new DirectExchange(PIX_EXCHANGE);
    }

    @Bean
    public Binding pixBinding(Queue pixQueue, DirectExchange pixExchange) {
        return BindingBuilder.bind(pixQueue).to(pixExchange).with(PIX_QUEUE);
    }
}
