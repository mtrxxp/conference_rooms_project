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

@Component
@Order(2)
public class ManagerDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ManagerDataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String managerEmail;
    private final String managerPassword;

    public ManagerDataInitializer(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${app.manager.email:manager@conference.local}") String managerEmail,
                                  @Value("${app.manager.password:Manager123!}") String managerPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.managerEmail = managerEmail;
        this.managerPassword = managerPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(managerEmail)) {
            return;
        }

        User manager = new User();
        manager.setFirstName("System");
        manager.setLastName("Manager");
        manager.setEmail(managerEmail);
        manager.setPassword(passwordEncoder.encode(managerPassword));
        manager.setRole(Role.MANAGER);

        userRepository.save(manager);
        log.info("Default manager account created: {}", managerEmail);
    }
}
