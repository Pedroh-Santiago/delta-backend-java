package br.com.deltaglobalbank.internal_treasury.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class TefPublisher {

    private final RabbitTemplate rabbitTemplate;

    public TefPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishApproval(UUID id) {
        Map<String, String> message = Map.of(
            "id", id.toString(),
            "approvedAt", Instant.now().toString()
        );
        rabbitTemplate.convertAndSend(
            TefRabbitMQConfig.TEF_EXCHANGE,
            TefRabbitMQConfig.TEF_QUEUE,
            message
        );
    }
}
