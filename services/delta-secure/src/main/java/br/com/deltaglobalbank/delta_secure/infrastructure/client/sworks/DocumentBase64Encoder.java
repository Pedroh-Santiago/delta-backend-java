package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import java.util.Base64;
import java.util.regex.Pattern;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksInvalidDocument;
import org.springframework.stereotype.Component;

@Component
public class DocumentBase64Encoder {

    private static final Pattern DATA_URI_PREFIX = Pattern.compile("^\\s*data:", Pattern.CASE_INSENSITIVE);

    public String encode(byte[] content) {
        if (content.length == 0) {
            throw new SWorksInvalidDocument("conteúdo do documento está vazio");
        }
        String value = Base64.getEncoder().encodeToString(content);
        requirePure(value);
        return value;
    }

    public void requirePure(String value) {
        if (value.isBlank()) {
            throw new SWorksInvalidDocument("base64 do documento está vazio");
        }
        if (DATA_URI_PREFIX.matcher(value).find()) {
            throw new SWorksInvalidDocument("base64 do documento não pode vir com prefixo data:, envie a string pura");
        }
        try {
            Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException ex) {
            throw new SWorksInvalidDocument("base64 do documento está malformado", ex);
        }
    }
}
