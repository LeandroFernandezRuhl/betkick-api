package com.leandroruhl.betkickapi.dto.security;

import lombok.Data;

/**
 * DTO representing the credentials of a user in a login attempt.
 */
@Data
public class CredentialsDto {
    private String login;
    private char[] password;
}
