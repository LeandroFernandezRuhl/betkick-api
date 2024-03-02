package com.example.betkickapi.service.user;


import com.example.betkickapi.dto.internal_api.UserBetSummary;
import com.example.betkickapi.dto.security.CredentialsDto;
import com.example.betkickapi.dto.security.SignUpDto;
import com.example.betkickapi.dto.security.UserDto;
import com.example.betkickapi.exception.AccountBalanceException;
import com.example.betkickapi.exception.EntityNotFoundException;
import com.example.betkickapi.exception.InvalidPasswordException;
import com.example.betkickapi.exception.UsernameAlreadyExistsException;
import com.example.betkickapi.model.User;
import com.example.betkickapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The {@code UserServiceImpl} class implements the {@link UserService} interface
 * and provides concrete implementations for managing user-related operations.
 * This class utilizes a {@link UserRepository} for interacting with user data.
 */
@AllArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation retrieves the user leaderboard by invoking the
     * {@link UserRepository#findEarningsAndBets()} method.
     *
     * @return A list of {@link UserBetSummary} objects representing the user leaderboard.
     */
    @Override
    @Cacheable(value = "footballDataCache", key = "'leaderboard'")
    public List<UserBetSummary> getUserLeaderboard() {
        return userRepository.findEarningsAndBets();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation withdraws the specified amount from the user's account balance.
     * It checks if the user has sufficient funds and updates the user's balance accordingly.
     *
     * @param user   The user from whom the withdrawal is made.
     * @param amount The amount to be withdrawn.
     * @return The updated {@link User} object after the withdrawal.
     * @throws AccountBalanceException If the user does not have sufficient funds for the withdrawal.
     */
    @Override
    public User withdraw(User user, Double amount) {
        if (user.getAccountBalance() >= amount) {
            user.setAccountBalance(user.getAccountBalance() - amount);
            return userRepository.save(user);
        } else {
            throw new AccountBalanceException("Insufficient funds");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation deposits the specified amount into the user's account balance.
     * It checks if the amount is finite and does not exceed the maximum possible value
     * for a double, then updates the user's balance accordingly.
     *
     * @param user   The user into whom the deposit is made.
     * @param amount The amount to be deposited.
     * @return The updated {@link User} object after the deposit.
     * @throws IllegalArgumentException If adding the amount would exceed the maximum possible value.
     */
    @Override
    public User deposit(User user, Double amount) {
        double currentBalance = user.getAccountBalance();
        if (Double.isFinite(amount) && currentBalance <= Double.MAX_VALUE - amount) {
            user.setAccountBalance(currentBalance + amount);
            return userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Adding the amount would exceed the max possible value");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation finds a user by the specified ID using the
     * UserRepository.findById(String) method.
     *
     * @param id The unique identifier of the user.
     * @return The {@link User} object associated with the given ID.
     * @throws EntityNotFoundException If no user is found with the specified ID.
     */
    @Override
    public User findById(String id) {
        return userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException(User.class, "userId", id));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation saves the user details using the
     * {@link UserRepository#save(Object)} method.
     *
     * @param user The {@link User} object to be saved.
     * @return The saved {@link User} object.
     */
    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation logs in a user with the specified credentials.
     * It retrieves the user by the username and checks if the password matches.
     *
     * @param credentialsDto The credentials of the user.
     * @return The {@link UserDto} object representing the logged-in user.
     * @throws InvalidPasswordException If the password is invalid.
     */
    @Override
    public UserDto login(CredentialsDto credentialsDto) throws InvalidPasswordException {
        User user = userRepository.findByLogin(credentialsDto.getLogin())
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Username", credentialsDto.getLogin()));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword()))
            return userToDto(user);

        throw new InvalidPasswordException("Incorrect password");
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation registers a new user with the specified details.
     * It checks if the username is already taken and saves the user details.
     *
     * @param signUpDto The details of the user to be registered.
     * @return The {@link UserDto} object representing the registered user.
     * @throws UsernameAlreadyExistsException If the username already exists.
     */
    @Override
    public UserDto register(SignUpDto signUpDto) throws UsernameAlreadyExistsException {
        Optional<User> oUser = userRepository.findByLogin(signUpDto.getLogin());

        if (oUser.isPresent()) {
            throw new UsernameAlreadyExistsException(signUpDto.getLogin() + " is taken. Please choose another username");
        }

        User user = singUpDtoToUser(signUpDto);
        user.setAccountBalance(1000D);
        user.setBets(new ArrayList<>());
        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(signUpDto.getPassword())));

        User savedUser = userRepository.save(user);
        return userToDto(savedUser);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation finds a user by the specified login (username) using the
     * UserRepository.findByLogin(String) method.
     *
     * @param login The login of the user.
     * @return The {@link User} object associated with the given login.
     * @throws EntityNotFoundException If no user is found with the specified login.
     */
    @Override
    public User findByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Username", login));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation converts a {@link User} object to a {@link UserDto} object
     * using the ModelMapper library.
     *
     * @param user The user to be converted.
     * @return The {@link UserDto} object representing the user.
     */
    @Override
    public UserDto userToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    /**
     * Converts a {@link SignUpDto} object to a {@link User} object.
     *
     * @param signUpDto The sign-up details to be converted.
     * @return The {@link User} object representing the sign-up details.
     */
    private User singUpDtoToUser(SignUpDto signUpDto) {
        return modelMapper.map(signUpDto, User.class);
    }
}