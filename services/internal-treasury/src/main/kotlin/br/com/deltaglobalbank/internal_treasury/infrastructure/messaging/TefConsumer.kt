package br.com.deltaglobalbank.internal_treasury.infrastructure.messaging

import br.com.deltaglobalbank.internal_treasury.features.processTef.ProcessTefUseCase
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TefConsumer (
    private val processTefUseCase: ProcessTefUseCase
){
    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [TefRabbitMQConfig.TEF_QUEUE])
    fun onMessage(message: Map<String, String>) {
        val rawId = message["id"] ?: run {
            log.error("Mensagem recebida sem o campo 'id': $message")
            throw IllegalArgumentException("Mensagem da fila TEF sem id")
        }
        val id = UUID.fromString(rawId)
        processTefUseCase.execute(id)
    }
}