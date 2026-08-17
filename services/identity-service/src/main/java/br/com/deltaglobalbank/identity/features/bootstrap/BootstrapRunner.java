package br.com.deltaglobalbank.identity.features.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapRunner.class);

    private final BootstrapUseCase bootstrapUseCase;

    public BootstrapRunner(BootstrapUseCase bootstrapUseCase) {
        this.bootstrapUseCase = bootstrapUseCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("bootstrap")) {
            return;
        }
        BootstrapResult result = bootstrapUseCase.execute();

        log.info("");
        log.info("╔══════════════════════════════════════════════════════════════════╗");
        log.info("║                  BOOTSTRAP CONCLUÍDO                             ║");
        log.info("╠══════════════════════════════════════════════════════════════════╣");
        log.info("║ Tenant criado:                                                   ║");
        log.info("║   ID:    " + result.tenantId() + "                  ║");
        log.info("║   Slug:  " + result.tenantSlug() + "                                          ║");
        log.info("║                                                                  ║");
        log.info("║ Admin criado:                                                    ║");
        log.info("║   Email: " + result.adminEmail() + "                              ║");
        log.info("║   Senha: " + result.temporaryPassword() + "                       ║");
        log.info("║                                                                  ║");
        log.info("║ Signing key criada:                                              ║");
        log.info("║   KID: " + result.signingKeyId() + "                                 ║");
        log.info("╠══════════════════════════════════════════════════════════════════╣");
        log.info("║ ANOTE A SENHA AGORA. ELA NÃO SERÁ MOSTRADA NOVAMENTE.            ║");
        log.info("║ Você será obrigado a trocá-la no primeiro login.                 ║");
        log.info("╚══════════════════════════════════════════════════════════════════╝");
        log.info("");

        System.exit(0);
    }
}
