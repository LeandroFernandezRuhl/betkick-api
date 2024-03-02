package com.example.betkickapi.config.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.betkickapi.dto.security.UserDto;
import com.example.betkickapi.model.User;
import com.example.betkickapi.service.user.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;

/**
 * Authentication provider class responsible for handling the creation and validation of JSON Web Tokens (JWT)
 * for user authentication.
 */
@RequiredArgsConstructor
@Component
public class UserAuthenticationProvider {

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    private final UserService userService;

    /**
     * Initializes the {@code secretKey} by encoding it using Base64 during the post-construction phase.
     * This helps avoid having the raw secret key available in the JVM.
     */
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    /**
     * Creates a JWT token for the provided user information.
     *
     * @param user The user information for whom the token is created.
     * @return A JWT token as a string.
     */
    public String createToken(UserDto user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000); // 1 hour

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withSubject(user.getLogin())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("firstName", user.getFirstName())
                .withClaim("lastName", user.getLastName())
                .sign(algorithm);
    }

    /**
     * Validates a JWT token and returns an Authentication object for the user.
     *
     * @param token The JWT token to be validated.
     * @return An Authentication object for the validated user.
     */
    public Authentication validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        JWTVerifier verifier = JWT.require(algorithm)
                .build();

        DecodedJWT decoded = verifier.verify(token);

        UserDto user = UserDto.builder()
                .login(decoded.getSubject())
                .firstName(decoded.getClaim("firstName").asString())
                .lastName(decoded.getClaim("lastName").asString())
                .build();

        return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    }

    /**
     * Strongly validates a JWT token by performing a user lookup from the database and returns an Authentication
     * object for the user.
     *
     * @param token The JWT token to be strongly validated.
     * @return An Authentication object for the strongly validated user.
     */
    public Authentication validateTokenStrongly(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        JWTVerifier verifier = JWT.require(algorithm)
                .build();

        DecodedJWT decoded = verifier.verify(token);

        User user = userService.findByLogin(decoded.getSubject());

        return new UsernamePasswordAuthenticationToken(userService.userToDto(user), null, Collections.emptyList());
    }
}