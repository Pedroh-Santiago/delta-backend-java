package br.com.deltaglobalbank.internal_treasury.infrastructure.messaging

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class PixPublisher (
    private val rabbitTemplate: RabbitTemplate,
) {
    fun publishApproval(id: UUID){
        val message = mapOf(
            "id" to id.toString(),
            "approvedAt" to Instant.now().toString(),
        )
        rabbitTemplate.convertAndSend(
            PixRabbitMQConfig.PIX_EXCHANGE,
            PixRabbitMQConfig.PIX_QUEUE,
            message
        )
    }
}