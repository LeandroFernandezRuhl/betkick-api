package com.example.betkickapi.controller;

import com.example.betkickapi.model.User;
import com.example.betkickapi.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;

import static java.util.Map.of;

/**
 * The UserController class is a Spring Web MVC controller responsible for handling user-related endpoints.
 */
@RestController
@Validated
@Slf4j
public class UserController {

    private final ClientRegistration registration;
    private final UserService userService;

    /**
     * Constructs a new {@code UserController} with the specified {@code registrations} and {@code userService}.
     *
     * @param registrations The client registration repository.
     * @param userService   The user service.
     */
    public UserController(ClientRegistrationRepository registrations, UserService userService) {
        this.registration = registrations.findByRegistrationId("okta");
        this.userService = userService;
    }

    /**
     * Retrieves the account balance of a user with the specified user ID.
     *
     * @param userId The user ID.
     * @return A {@link ResponseEntity} containing the user's account balance.
     */
    @GetMapping("/api/user/balance")
    public ResponseEntity<Double> getUserBalance(@RequestParam String userId) {
        try {
            String decodedUserId = URLDecoder.decode(userId, "UTF-8");
            Double balance = userService.findById(decodedUserId).getAccountBalance();
            return ResponseEntity.ok(balance);
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
    @PostMapping("/api/user/withdraw")
    @Transactional
    public ResponseEntity<Double> withdraw(@RequestBody @NotNull @Positive Double amount, @RequestParam @NotNull String userId) {
        log.info("Request to withdraw received!");
        User user = userService.findById(userId);
        User updatedUser = userService.withdraw(user, amount);
        return ResponseEntity.ok(updatedUser.getAccountBalance());
    }

    /**
     * Handles deposit requests for a user with the specified user ID.
     *
     * @param amount The deposit amount.
     * @param userId The user ID.
     * @return A {@link ResponseEntity} containing the updated account balance after deposit.
     */
    @PostMapping("/api/user/deposit")
    @Transactional
    public ResponseEntity<Double> deposit(@RequestBody @NotNull @Positive Double amount, @RequestParam String userId) {
        log.info("Request to retrieve deposit received!");
        User user = userService.findById(userId);
        User updatedUser = userService.deposit(user, amount);
        return ResponseEntity.ok(updatedUser.getAccountBalance());
    }

    /**
     * Retrieves information about the currently authenticated user.
     *
     * @param user The authenticated user.
     * @return A {@link ResponseEntity} containing user attributes.
     */
    @GetMapping("/api/user")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            // Get the Auth0 user ID
            String auth0UserId = user.getAttribute("sub");

            if (!userService.existsById(auth0UserId)) {
                // If the user doesn't exist, create a new User entity and store Auth0 user ID
                User applicationUser = new User();
                applicationUser.setId(auth0UserId);
                applicationUser.setName(user.getAttribute("nickname"));
                applicationUser.setEmail(user.getAttribute("email"));
                // Users start with $1000 balance
                applicationUser.setAccountBalance(1000d);

                // Save this user in the application's database
                userService.saveUser(applicationUser);
            }
            return ResponseEntity.ok().body(user.getAttributes());
        }
    }

    /**
     * Initiates the logout process for the authenticated user.
     *
     * @param request The HTTP servlet request.
     * @return A {@link ResponseEntity} containing the logout URL.
     */
    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Send logout URL to client so they can initiate logout
        var issuerUri = registration.getProviderDetails().getIssuerUri();
        var originUrl = request.getHeader(HttpHeaders.ORIGIN);
        Object[] params = {issuerUri, registration.getClientId(), originUrl};
        var logoutUrl = MessageFormat.format("{0}v2/logout?client_id={1}&returnTo={2}", params);
        request.getSession().invalidate();
        return ResponseEntity.ok().body(of("logoutUrl", logoutUrl));
    }
}
