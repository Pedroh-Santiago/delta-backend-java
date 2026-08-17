package br.com.deltaglobalbank.delta_secure.infrastructure.client.heroseguros;

import java.nio.charset.StandardCharsets;

import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosRequestRejected;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.HeroSegurosUnavailable;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;

public class HeroSegurosErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder.Default default_ = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = null;
        if (response.body() != null) {
            try {
                body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            } catch (java.io.IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        if (response.status() >= 500) {
            return new HeroSegurosUnavailable();
        } else if (response.status() >= 400 && response.status() <= 499) {
            return new HeroSegurosRequestRejected(
                "Hero Seguros retornou status " + response.status() + " em " + methodKey + ": "
                    + (body != null ? body : "sem corpo")
            );
        } else {
            return default_.decode(methodKey, response);
        }
    }
}
