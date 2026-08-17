package br.com.deltaglobalbank.identity.features.auth.login;

import br.com.deltaglobalbank.identity.domain.user.User;
import br.com.deltaglobalbank.identity.domain.user.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FailedLoginRecorder {

    private final UserRepository userRepository;

    public FailedLoginRecorder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(User user) {
        userRepository.save(user);
    }
}
