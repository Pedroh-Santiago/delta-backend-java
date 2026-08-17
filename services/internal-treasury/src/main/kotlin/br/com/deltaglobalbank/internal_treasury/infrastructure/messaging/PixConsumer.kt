package br.com.deltaglobalbank.internal_treasury.infrastructure.messaging

import br.com.deltaglobalbank.internal_treasury.features.processPix.ProcessPixUseCase
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PixConsumer (
    private val processPixUseCase: ProcessPixUseCase
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [PixRabbitMQConfig.PIX_QUEUE])
    fun onMessage(message: Map<String, String>) {
        val rawId = message["id"] ?: run {
            log.error("Mensagem recebida sem o campo 'id': $message")
            throw IllegalArgumentException("Mensagem na fila PIX sem id")
        }
        val id = UUID.fromString(message["id"])
        processPixUseCase.execute(id)
    }
}