package com.leandroruhl.betkickapi.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Configuration class for setting up password encoding.
 */
@Component
public class PasswordConfiguration {

    /**
     * Creates a BCrypt password encoder bean.
     *
     * @return A BCrypt password encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
