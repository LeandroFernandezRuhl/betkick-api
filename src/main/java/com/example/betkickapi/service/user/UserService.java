package com.example.betkickapi.service.user;

import com.example.betkickapi.dto.internal_api.UserBetSummary;
import com.example.betkickapi.dto.security.CredentialsDto;
import com.example.betkickapi.dto.security.SignUpDto;
import com.example.betkickapi.dto.security.UserDto;
import com.example.betkickapi.exception.InvalidPasswordException;
import com.example.betkickapi.exception.UsernameAlreadyExistsException;
import com.example.betkickapi.model.User;

import java.util.List;

/**
 * The UserService interface provides methods for managing user-related operations
 * such as checking user existence, retrieving user leaderboard information,
 * withdrawing and depositing funds, finding users by ID, and saving user details.
 */
public interface UserService {

    /**
     * Retrieves the leaderboard information for users.
     *
     * @return A list of {@link UserBetSummary} objects representing the user leaderboard.
     */
    List<UserBetSummary> getUserLeaderboard();

    /**
     * Withdraws a specified amount from the user's account balance.
     *
     * @param user   The user from whom the withdrawal is made.
     * @param amount The amount to be withdrawn.
     * @return The updated {@link User} object after the withdrawal.
     * @throws com.example.betkickapi.exception.AccountBalanceException If the user does not have sufficient funds for the withdrawal.
     */
    User withdraw(User user, Double amount);

    /**
     * Deposits a specified amount into the user's account balance.
     *
     * @param user   The user into whom the deposit is made.
     * @param amount The amount to be deposited.
     * @return The updated {@link User} object after the deposit.
     * @throws IllegalArgumentException If the amount exceeds the account balance limit.
     * @see User
     */
    User deposit(User user, Double amount);

    /**
     * Finds a user by the specified ID.
     *
     * @param id The unique identifier of the user.
     * @return The {@link User} object associated with the given ID.
     * @throws com.example.betkickapi.exception.EntityNotFoundException If no user is found.
     */
    User findById(String id);

    /**
     * Saves the user details.
     *
     * @param user The {@link User} object to be saved.
     * @return The saved {@link User} object.
     */
    User saveUser(User user);

    /**
     * Logs in a user with the specified credentials.
     *
     * @param credentialsDto The credentials of the user.
     * @return The {@link UserDto} object representing the logged-in user.
     * @throws InvalidPasswordException If the password is invalid.
     */
    UserDto login(CredentialsDto credentialsDto) throws InvalidPasswordException;

    /**
     * Registers a new user with the specified details.
     *
     * @param signUpDto The details of the user to be registered.
     * @return The {@link UserDto} object representing the registered user.
     * @throws UsernameAlreadyExistsException If the username already exists.
     */
    UserDto register(SignUpDto signUpDto) throws UsernameAlreadyExistsException;

    /**
     * Finds a user by the specified login (username).
     *
     * @param login The login of the user.
     * @return The {@link User} object associated with the given login.
     */
    User findByLogin(String login);

    /**
     * Converts a {@link User} object to a {@link UserDto} object.
     *
     * @param user The user to be converted.
     * @return The {@link UserDto} object representing the user.
     */
    UserDto userToDto(User user);
}


