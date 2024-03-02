package com.example.betkickapi.controller;

import com.example.betkickapi.config.security.UserAuthenticationProvider;
import com.example.betkickapi.dto.security.CredentialsDto;
import com.example.betkickapi.dto.security.SignUpDto;
import com.example.betkickapi.dto.security.UserDto;
import com.example.betkickapi.exception.InvalidPasswordException;
import com.example.betkickapi.exception.UsernameAlreadyExistsException;
import com.example.betkickapi.model.User;
import com.example.betkickapi.service.user.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Controller class handling authentication and user-related operations through RESTful endpoints.
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api")
public class AuthController {
    private final UserService userService;
    private final UserAuthenticationProvider userAuthenticationProvider;

    /**
     * Handles user login requests and returns a JWT token upon successful authentication.
     *
     * @param credentialsDto The user credentials for login.
     * @return A {@link ResponseEntity} containing the user details and JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CredentialsDto credentialsDto) {
        UserDto user;
        try {
            user = userService.login(credentialsDto);
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
        user.setToken(userAuthenticationProvider.createToken(user));
        return ResponseEntity.ok(user);
    }

    /**
     * Handles user registration requests and returns a JWT token upon successful registration.
     *
     * @param signUpDto The user details for registration.
     * @return A {@link ResponseEntity} containing the user details and JWT token.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SignUpDto signUpDto) {
        UserDto user;
        try {
            user = userService.register(signUpDto);
        } catch (UsernameAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        user.setToken(userAuthenticationProvider.createToken(user));
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves the account balance of a user with the specified user ID.
     *
     * @param userId The user ID.
     * @return A {@link ResponseEntity} containing the user's account balance.
     */
    @GetMapping("/user/balance")
    public ResponseEntity<?> getUserBalance(@RequestParam String userId) {
        try {
            String decodedUserId = URLDecoder.decode(userId, "UTF-8");
            Double balance = userService.findById(decodedUserId).getAccountBalance();
            return ResponseEntity.ok(balance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID");
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Handles withdrawal requests for a user with the specified user ID.
     *
     * @param amount The withdrawal amount.
     * @param userId The user ID.
     * @return A {@link ResponseEntity} containing the updated account balance after withdrawal.
     */
    @PostMapping("/user/withdraw")
    @Transactional
    public ResponseEntity<?> withdraw(@RequestBody @NotNull @Positive Double amount, @RequestParam @NotNull String userId) {
        log.info("Request to withdraw received!");
        try {
            User user = userService.findById(userId);
            User updatedUser = userService.withdraw(user, amount);
            return ResponseEntity.ok(updatedUser.getAccountBalance());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID");
        }
    }

    /**
     * Handles deposit requests for a user with the specified user ID.
     *
     * @param amount The deposit amount.
     * @param userId The user ID.
     * @return A {@link ResponseEntity} containing the updated account balance after deposit.
     */
    @PostMapping("/user/deposit")
    @Transactional
    public ResponseEntity<?> deposit(@RequestBody @NotNull @Positive Double amount, @RequestParam String
            userId) {
        log.info("Request to retrieve deposit received!");
        try {
            User user = userService.findById(userId);
            User updatedUser = userService.deposit(user, amount);
            return ResponseEntity.ok(updatedUser.getAccountBalance());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID");
        }
    }
}
