package com.example.betkickapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuration class for security settings in the application.
 * <br>
 * <br>
 * This class defines security configurations such as authorization rules, OAuth2 login setup,
 * CSRF protection, and JWT resource server settings.
 * <br>
 * <br>
 * The application provides public access to certain endpoints like home page, static resources,
 * and specific API endpoints related to standings, leaderboard, user information, active competitions,
 * competitions with standings, and matches. All other requests require authentication.
 * <br>
 * <br>
 * OAuth2 login is configured with default settings, and a JWT resource server is enabled for secure
 * access to protected resources.
 * <br>
 * <br>
 * CSRF protection is implemented using a custom filter and token repository. The configuration ensures
 * that CSRF tokens are sent as cookies and handled appropriately for security.
 */
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/", "/index.html", "*.ico", "*.css", "*.js", "/api/standings",
                                "/api/leaderboard", "/api/user", "/api/active-competitions",
                                "/api/competitions-with-standings", "/api/matches").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(withDefaults())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(withDefaults()))
                .csrf((csrf) -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterAfter(new CookieCsrfFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }
}