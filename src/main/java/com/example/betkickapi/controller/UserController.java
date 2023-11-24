package com.example.betkickapi.controller;

import com.example.betkickapi.model.User;
import com.example.betkickapi.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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

import java.text.MessageFormat;

import static java.util.Map.of;

@RestController
@Validated
@Slf4j
public class UserController {
    private final ClientRegistration registration;
    private final UserService userService;

    public UserController(ClientRegistrationRepository registrations, UserService userService) {
        this.registration = registrations.findByRegistrationId("okta");
        this.userService = userService;
    }

    @GetMapping("/api/user/balance")
    public ResponseEntity<Double> getUserBalance(String userId) {
        return ResponseEntity.ok(userService.findById(userId).getAccountBalance());
    }

    @PostMapping("/api/user/withdraw")
    @Transactional
    public ResponseEntity<Double> withdraw(@RequestBody @NotNull @Positive Double amount, @RequestParam @NotNull String userId) {
        log.info("Request to withdraw received!");
        User user = userService.findById(userId);
        User updatedUser = userService.decrementUserBalance(user, amount);
        return ResponseEntity.ok(updatedUser.getAccountBalance());
    }

    @PostMapping("/api/user/deposit")
    @Transactional
    public ResponseEntity<Double> deposit(@RequestBody @NotNull @Positive Double amount, @RequestParam String userId) {
        log.info("Request to retrieve deposit received!");
        User user = userService.findById(userId);
        User updatedUser = userService.incrementUserBalance(user, amount);
        return ResponseEntity.ok(updatedUser.getAccountBalance());
    }

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
                applicationUser.setName(user.getAttribute("name"));
                applicationUser.setEmail(user.getAttribute("email"));
                // users start with $1000 balance
                applicationUser.setAccountBalance(1000d);

                // Save this user in the application's database
                userService.saveUser(applicationUser);
            }
            return ResponseEntity.ok().body(user.getAttributes());
        }
    }

    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // send logout URL to client so they can initiate logout
        var issuerUri = registration.getProviderDetails().getIssuerUri();
        var originUrl = request.getHeader(HttpHeaders.ORIGIN);
        Object[] params = {issuerUri, registration.getClientId(), originUrl};
        var logoutUrl = MessageFormat.format("{0}v2/logout?client_id={1}&returnTo={2}", params);
        request.getSession().invalidate();
        return ResponseEntity.ok().body(of("logoutUrl", logoutUrl));
    }
}