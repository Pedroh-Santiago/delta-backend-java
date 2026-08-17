package br.com.deltaglobalbank.identity.features.jwks;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus;
import br.com.deltaglobalbank.identity.infrastructure.security.jwt.PemConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JwksUseCase {

    private final SigningKeyRepository signingKeyRepository;
    private final PemConverter pemConverter;

    public JwksUseCase(SigningKeyRepository signingKeyRepository, PemConverter pemConverter) {
        this.signingKeyRepository = signingKeyRepository;
        this.pemConverter = pemConverter;
    }

    @Transactional(readOnly = true)
    public JwkResponse execute() {
        List<SigningKey> activeKeys = signingKeyRepository.findAllByStatus(SigningKeyStatus.ACTIVE);
        List<SigningKey> retiredKeys = signingKeyRepository.findAllByStatus(SigningKeyStatus.RETIRED);

        List<Jwk> jwks = new ArrayList<>();
        activeKeys.forEach(k -> jwks.add(toJwk(k)));
        retiredKeys.forEach(k -> jwks.add(toJwk(k)));

        return new JwkResponse(jwks);
    }

    private Jwk toJwk(SigningKey signingKey) {
        RSAPublicKey publicKey = pemConverter.toPublicKey(signingKey.getPublicKey());

        return new Jwk(
            "RSA",
            "sig",
            signingKey.getAlgorithm(),
            signingKey.getKid(),
            encodeUnsignedBase64Url(publicKey.getModulus().toByteArray()),
            encodeUnsignedBase64Url(publicKey.getPublicExponent().toByteArray())
        );
    }

    private String encodeUnsignedBase64Url(byte[] bytes) {
        byte[] stripped = bytes.length > 0 && bytes[0] == 0
            ? java.util.Arrays.copyOfRange(bytes, 1, bytes.length)
            : bytes;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(stripped);
    }
}
