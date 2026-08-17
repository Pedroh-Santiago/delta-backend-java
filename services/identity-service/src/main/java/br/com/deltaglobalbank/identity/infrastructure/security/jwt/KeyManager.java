package br.com.deltaglobalbank.identity.infrastructure.security.jwt;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import br.com.deltaglobalbank.identity.infrastructure.persistence.entities.SigningKeyEntity;
import br.com.deltaglobalbank.identity.infrastructure.persistence.repositories.JpaSigningKeyRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class KeyManager {

    private final JpaSigningKeyRepository signingKeyRepository;
    private final PemConverter pemConverter;

    private final ConcurrentHashMap<String, SigningKeyMaterial> cache = new ConcurrentHashMap<>();

    public KeyManager(JpaSigningKeyRepository signingKeyRepository, PemConverter pemConverter) {
        this.signingKeyRepository = signingKeyRepository;
        this.pemConverter = pemConverter;
    }

    @PostConstruct
    public void warmUp() {
        refresh();
    }

    public void refresh() {
        cache.clear();
        signingKeyRepository.findAll().forEach(entity -> cache.put(entity.getKid(), toMaterial(entity)));
    }

    public SigningKeyMaterial getActiveSigningKey() {
        List<SigningKeyMaterial> active = cache.values().stream()
            .filter(it -> it.status().equals("active"))
            .toList();
        if (active.isEmpty()) {
            throw new IllegalStateException("Nenhuma signing key ativa encontrada. Bootstrap foi executado?");
        }
        if (active.size() != 1) {
            throw new IllegalStateException("Múltiplas signing keys ativas encontradas. Estado inconsistente.");
        }
        return active.get(0);
    }

    public SigningKeyMaterial getByKid(String kid) {
        SigningKeyMaterial cached = cache.get(kid);
        if (cached != null) {
            return cached;
        }
        SigningKeyEntity entity = signingKeyRepository.findByKid(kid);
        if (entity == null) {
            return null;
        }
        SigningKeyMaterial material = toMaterial(entity);
        cache.put(kid, material);
        return material;
    }

    public List<SigningKeyMaterial> getAllUsable() {
        Set<String> usableStatuses = Set.of("active", "retired");
        return cache.values().stream().filter(it -> usableStatuses.contains(it.status())).toList();
    }

    private SigningKeyMaterial toMaterial(SigningKeyEntity entity) {
        return new SigningKeyMaterial(
            entity.getKid(),
            pemConverter.toPrivateKey(entity.getPrivateKey()),
            pemConverter.toPublicKey(entity.getPublicKey()),
            entity.getStatus()
        );
    }
}
