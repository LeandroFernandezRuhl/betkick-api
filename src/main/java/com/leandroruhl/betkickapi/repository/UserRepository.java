package com.leandroruhl.betkickapi.repository;

import com.leandroruhl.betkickapi.dto.internal_api.UserBetSummary;
import com.leandroruhl.betkickapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The {@code UserRepository} interface extends the {@link JpaRepository} for managing {@link User} entities.
 * It also provides a custom query for retrieving earnings and bets summary.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Retrieves a list of {@link UserBetSummary} objects representing earnings and bets.
     *
     * @return A list of {@link UserBetSummary} objects.
     */
    @Query(nativeQuery = true)
    List<UserBetSummary> findEarningsAndBets();

    Optional<User> findByLogin(String login);
}

