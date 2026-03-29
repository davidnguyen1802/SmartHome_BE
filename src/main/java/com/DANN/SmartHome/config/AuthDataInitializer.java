package com.DANN.SmartHome.config;

import com.DANN.SmartHome.domain.entity.UserEntity;
import com.DANN.SmartHome.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${auth.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${auth.seed.username:admin}")
    private String seedUsername;

    @Value("${auth.seed.password:admin123}")
    private String seedPassword;

    @Override
    @SuppressWarnings("NullableProblems")
    public void run(String... args) {
        if (!seedEnabled || userRepository.existsByUsernameIgnoreCase(seedUsername)) {
            return;
        }

        UserEntity user = new UserEntity();
        user.setUsername(seedUsername);
        user.setPasswordHash(passwordEncoder.encode(seedPassword));
        user.setEnabled(true);

        userRepository.save(user);
    }
}

