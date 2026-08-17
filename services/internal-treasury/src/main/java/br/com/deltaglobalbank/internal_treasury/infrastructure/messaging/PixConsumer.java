package br.com.deltaglobalbank.internal_treasury.infrastructure.messaging;

import br.com.deltaglobalbank.internal_treasury.features.processPix.ProcessPixUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Component
public class PixConsumer {

    private static final Logger log = LoggerFactory.getLogger(PixConsumer.class);

    private final ProcessPixUseCase processPixUseCase;

    public PixConsumer(ProcessPixUseCase processPixUseCase) {
        this.processPixUseCase = processPixUseCase;
    }

    @RabbitListener(queues = PixRabbitMQConfig.PIX_QUEUE)
    public void onMessage(Map<String, String> message) {
        String rawId = message.get("id");
        if (rawId == null) {
            log.error("Mensagem recebida sem o campo 'id': " + message);
            throw new IllegalArgumentException("Mensagem na fila PIX sem id");
        }
        UUID id = UUID.fromString(message.get("id"));
        processPixUseCase.execute(id);
    }
}
