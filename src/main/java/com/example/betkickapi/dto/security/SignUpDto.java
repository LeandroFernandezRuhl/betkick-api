package com.example.betkickapi.dto.security;

import lombok.Data;

/**
 * DTO representing user info on sign up.
 */
@Data
public class SignUpDto {
    private String firstName;
    private String lastName;
    private String login;
    private char[] password;
}
