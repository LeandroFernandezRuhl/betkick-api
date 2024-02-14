package com.example.betkickapi.repository;

import com.example.betkickapi.dto.internal_api.UserBetSummary;
import com.example.betkickapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The {@code UserRepository} interface extends the {@link JpaRepository} for managing {@link User} entities.
 * It also provides a custom query for retrieving earnings and bets summary.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Retrieves a list of {@link UserBetSummary} objects representing earnings and bets.
     *
     * @return A list of {@link UserBetSummary} objects.
     */
    @Query(nativeQuery = true)
    List<UserBetSummary> findEarningsAndBets();
}

