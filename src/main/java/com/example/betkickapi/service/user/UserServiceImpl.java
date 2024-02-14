package com.example.betkickapi.service.user;


import com.example.betkickapi.dto.internal_api.UserBetSummary;
import com.example.betkickapi.exception.AccountBalanceException;
import com.example.betkickapi.exception.EntityNotFoundException;
import com.example.betkickapi.model.User;
import com.example.betkickapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * {@inheritDoc}
     * <p>
     * This implementation checks if a user with the specified ID exists by invoking
     * the UserRepository.existsById(String) method.
     *
     * @param id The unique identifier of the user.
     * @return {@code true} if a user with the given ID exists; {@code false} otherwise.
     */
    @Override
    public Boolean existsById(String id) {
        return userRepository.existsById(id);
    }

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
        return userRepository.findById(id)
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
}