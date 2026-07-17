package com.mtrxxp.backend.config;

import com.mtrxxp.backend.user.Role;
import com.mtrxxp.backend.user.User;
import com.mtrxxp.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a default administrator account on startup if it does not exist yet.
 * Credentials are configurable via app.admin.email and app.admin.password.
 */
@Component
@Order(1)
public class AdminDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminDataInitializer(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.admin.email:admin@conference.local}") String adminEmail,
                                @Value("${app.admin.password:Admin123!}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User admin = new User();
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);
        log.info("Default admin account created: {}", adminEmail);
    }
}
