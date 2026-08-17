package br.com.deltaglobalbank.internal_treasury.features.processPix;

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PixGateway;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.PixNotFoundException;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class ProcessPixUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPixUseCase.class);

    private final MakePixRepository makePixRepository;
    private final PixGateway pixGateway;

    public ProcessPixUseCase(MakePixRepository makePixRepository, PixGateway pixGateway) {
        this.makePixRepository = makePixRepository;
        this.pixGateway = pixGateway;
    }

    @Transactional
    public void execute(UUID id) {
        MakePix transferece = makePixRepository.findById(id);
        if (transferece == null) {
            log.error("PIX " + id + " não foi encontrado na fila");
            throw new PixNotFoundException(id);
        }

        if (transferece.status() == PaymentsStatus.PAID) {
            log.info("PIX " + id + " já está pago");
            return;
        }

        long transactionId = pixGateway.transferPix(
            transferece.accountId(),
            transferece.recipientInstitutionCode(),
            transferece.recipientBranchCode(),
            transferece.recipientAccountNumber(),
            transferece.recipientAccountType(),
            transferece.recipientName(),
            transferece.operationAmount()
        );

        log.info("PIX " + id + " pago na Paysmart (trasactionId=" + transactionId + ") persistindo status localmente");

        transferece.markAsPaid(Instant.now());

        makePixRepository.save(transferece);

        log.info("PIX " + id + " persistindo como PAGO com sucesso");
    }
}
