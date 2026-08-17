package br.com.deltaglobalbank.identity.features.signingKeys.listSigningKeys;

import java.util.ArrayList;
import java.util.List;

import br.com.deltaglobalbank.identity.domain.token.SigningKey;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyRepository;
import br.com.deltaglobalbank.identity.domain.token.SigningKeyStatus;
import br.com.deltaglobalbank.identity.features.signingKeys.SigningKeyViewMapper;
import br.com.deltaglobalbank.identity.features.signingKeys.rotateSigningKeys.SigningKeyView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListSigningKeysUseCase {

    private final SigningKeyRepository signingKeyRepository;

    public ListSigningKeysUseCase(SigningKeyRepository signingKeyRepository) {
        this.signingKeyRepository = signingKeyRepository;
    }

    @Transactional(readOnly = true)
    public ListSigningKeysResponse execute() {
        List<SigningKey> all = new ArrayList<>();
        for (SigningKeyStatus status : SigningKeyStatus.values()) {
            all.addAll(signingKeyRepository.findAllByStatus(status));
        }
        List<SigningKeyView> items = all.stream().map(SigningKeyViewMapper::toView).toList();
        return new ListSigningKeysResponse(items);
    }
}
