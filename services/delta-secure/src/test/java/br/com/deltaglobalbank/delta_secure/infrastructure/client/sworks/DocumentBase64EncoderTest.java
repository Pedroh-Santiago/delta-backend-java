package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksInvalidDocument;
import org.junit.jupiter.api.Test;

class DocumentBase64EncoderTest {

    private final DocumentBase64Encoder encoder = new DocumentBase64Encoder();

    @Test
    void codificaOConteudoEDecodificaDeVoltaParaOsBytesOriginais() {
        byte[] original = "%PDF-1.5 conteúdo do termo de adesão".getBytes(StandardCharsets.UTF_8);

        String encoded = encoder.encode(original);

        assertArrayEquals(original, Base64.getDecoder().decode(encoded));
    }

    @Test
    void naoInsereQuebrasDeLinhaEmConteudoGrande() {
        byte[] original = new byte[8_192];
        for (int i = 0; i < original.length; i++) {
            original[i] = (byte) (i % 256);
        }

        String encoded = encoder.encode(original);

        assertFalse(encoded.contains("\n"), "base64 com quebra de linha é rejeitado pelo decoder estrito");
        assertFalse(encoded.contains("\r"));
    }

    @Test
    void rejeitaConteudoVazio() {
        assertThrows(SWorksInvalidDocument.class, () -> encoder.encode(new byte[0]));
    }

    @Test
    void rejeitaBase64ComPrefixoData() {
        String comPrefixo = "data:application/pdf;base64," + Base64.getEncoder().encodeToString("pdf".getBytes(StandardCharsets.UTF_8));

        assertThrows(SWorksInvalidDocument.class, () -> encoder.requirePure(comPrefixo));
    }

    @Test
    void rejeitaBase64ComPrefixoDataPrecedidoDeEspacos() {
        assertThrows(SWorksInvalidDocument.class, () -> encoder.requirePure("  DATA:application/pdf;base64,cGRm"));
    }

    @Test
    void rejeitaBase64Malformado() {
        assertThrows(SWorksInvalidDocument.class, () -> encoder.requirePure("nao é base64 válido!!"));
    }

    @Test
    void rejeitaBase64Vazio() {
        assertThrows(SWorksInvalidDocument.class, () -> encoder.requirePure("   "));
    }

    @Test
    void aceitaBase64Puro() {
        encoder.requirePure(Base64.getEncoder().encodeToString("pdf".getBytes(StandardCharsets.UTF_8)));
    }
}
