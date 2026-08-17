package br.com.deltaglobalbank.identity.features.users.activateUser;

import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserNotFound;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActivateUserUseCase.class);

    private final UserRepository userRepository;

    public ActivateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void activateUser(UUID userId, UUID tenantId, UUID actorId) {
        User userExists = userRepository.findById(userId);
        if (userExists == null) {
            throw new UserNotFound();
        }
        if (!userExists.getTenantId().equals(tenantId)) {
            throw new UserNotFound();
        }
        userExists.activate();
        userRepository.save(userExists);
        log.info("user activated userId={} tenantId={} by={}", userId, tenantId, actorId);
    }
}
