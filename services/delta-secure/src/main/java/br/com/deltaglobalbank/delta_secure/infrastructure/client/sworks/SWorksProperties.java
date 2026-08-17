package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "sworks")
public record SWorksProperties(
    @DefaultValue("") String baseUrl,
    SWorksCredentials auth,
    @DefaultValue("true") boolean verifyAfterUpload,
    SWorksProcessSettings processo,
    SWorksDocumentSettings documento
) {
    public SWorksProperties {
        if (auth == null) {
            auth = new SWorksCredentials("", "", "password");
        }
        if (processo == null) {
            processo = new SWorksProcessSettings(null, null, null);
        }
        if (documento == null) {
            documento = new SWorksDocumentSettings(null);
        }
    }
}
