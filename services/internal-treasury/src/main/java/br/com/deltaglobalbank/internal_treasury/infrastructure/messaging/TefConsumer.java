package br.com.deltaglobalbank.internal_treasury.infrastructure.messaging;

import br.com.deltaglobalbank.internal_treasury.features.processTef.ProcessTefUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Component
public class TefConsumer {

    private static final Logger log = LoggerFactory.getLogger(TefConsumer.class);

    private final ProcessTefUseCase processTefUseCase;

    public TefConsumer(ProcessTefUseCase processTefUseCase) {
        this.processTefUseCase = processTefUseCase;
    }

    @RabbitListener(queues = TefRabbitMQConfig.TEF_QUEUE)
    public void onMessage(Map<String, String> message) {
        String rawId = message.get("id");
        if (rawId == null) {
            log.error("Mensagem recebida sem o campo 'id': " + message);
            throw new IllegalArgumentException("Mensagem da fila TEF sem id");
        }
        UUID id = UUID.fromString(rawId);
        processTefUseCase.execute(id);
    }
}
