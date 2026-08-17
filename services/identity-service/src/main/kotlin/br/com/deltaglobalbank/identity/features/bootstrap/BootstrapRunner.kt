package br.com.deltaglobalbank.identity.features.bootstrap

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class BootstrapRunner(private val bootstrapUseCase: BootstrapUseCase) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(this.javaClass)
    override fun run(args: ApplicationArguments) {
        if (!args.containsOption("bootstrap")) {
            return
        }
        val result = bootstrapUseCase.execute()

        log.info("")
        log.info("╔══════════════════════════════════════════════════════════════════╗")
        log.info("║                  BOOTSTRAP CONCLUÍDO                             ║")
        log.info("╠══════════════════════════════════════════════════════════════════╣")
        log.info("║ Tenant criado:                                                   ║")
        log.info("║   ID:    ${result.tenantId}                  ║")
        log.info("║   Slug:  ${result.tenantSlug}                                          ║")
        log.info("║                                                                  ║")
        log.info("║ Admin criado:                                                    ║")
        log.info("║   Email: ${result.adminEmail}                              ║")
        log.info("║   Senha: ${result.temporaryPassword}                       ║")
        log.info("║                                                                  ║")
        log.info("║ Signing key criada:                                              ║")
        log.info("║   KID: ${result.signingKeyId}                                 ║")
        log.info("╠══════════════════════════════════════════════════════════════════╣")
        log.info("║ ANOTE A SENHA AGORA. ELA NÃO SERÁ MOSTRADA NOVAMENTE.            ║")
        log.info("║ Você será obrigado a trocá-la no primeiro login.                 ║")
        log.info("╚══════════════════════════════════════════════════════════════════╝")
        log.info("")

        exitProcess(0)
    }
}