package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import java.nio.charset.StandardCharsets;

import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksAccessDenied;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksDocumentRejected;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksProcessNotFound;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksTokenExpired;
import br.com.deltaglobalbank.delta_secure.features.sworks.SWorksUnavailable;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;

public class SWorksErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder.Default default_ = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String body = null;
        if (response.body() != null) {
            try {
                body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            } catch (java.io.IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        String detail = (body == null || body.isBlank()) ? "sem corpo" : body;

        if (status == 401) {
            return new SWorksTokenExpired();
        } else if (status == 403) {
            return new SWorksAccessDenied(detail);
        } else if (status == 404) {
            return new SWorksProcessNotFound(detail);
        } else if (status >= 500) {
            return new SWorksUnavailable(status, detail);
        } else if (status >= 400 && status <= 499) {
            return new SWorksDocumentRejected(status, detail);
        } else {
            return default_.decode(methodKey, response);
        }
    }
}
