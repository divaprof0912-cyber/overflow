package com.overflow.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserService users;
    private final String name;
    private final String email;
    private final String password;

    public AdminBootstrap(UserService users,
                          @Value("${overflow.bootstrap.admin.name:}") String name,
                          @Value("${overflow.bootstrap.admin.email:}") String email,
                          @Value("${overflow.bootstrap.admin.password:}") String password) {
        this.users = users;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (name == null || name.isBlank() || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            log.warn("No initial administrator was configured. Set OVERFLOW_ADMIN_NAME, OVERFLOW_ADMIN_EMAIL, and OVERFLOW_ADMIN_PASSWORD in the local run configuration.");
            return;
        }
        if (users.createBootstrapAdmin(name, email, password)) {
            log.info("Created the initial administrator account from local environment settings.");
        } else {
            log.info("Admin bootstrap skipped because an account with that email already exists; no password was changed.");
        }
    }
}
