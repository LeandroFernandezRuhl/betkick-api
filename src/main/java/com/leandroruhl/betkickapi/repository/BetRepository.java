package com.leandroruhl.betkickapi.repository;

import com.leandroruhl.betkickapi.model.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * The {@code BetRepository} interface extends the {@link JpaRepository} for managing {@link Bet} entities.
 * It provides query methods for retrieving bets based on specific criteria with eager fetching of associated entities.
 */
@Repository
public interface BetRepository extends JpaRepository<Bet, Integer> {

    /**
     * Retrieves a list of bets for a given match ID with eager fetching of associated user entity.
     *
     * @param matchId The ID of the match.
     * @return A list of {@link Bet} objects with eager fetching of the associated user.
     */
    @Query("SELECT b FROM Bet b " +
            "LEFT JOIN FETCH b.user " +
            "WHERE b.match.id = :matchId")
    List<Bet> findByMatchId(@Param("matchId") Integer matchId);

    /**
     * Retrieves a list of bets for a given user ID with eager fetching of associated match and competition entities.
     *
     * @param userId The ID of the user.
     * @return A list of {@link Bet} objects with eager fetching of associated match and competition entities.
     */
    @Query("SELECT b FROM Bet b " +
            "LEFT JOIN FETCH b.match " +
            "LEFT JOIN FETCH b.match.competition " +
            "LEFT JOIN FETCH b.match.homeTeam " +
            "LEFT JOIN FETCH b.match.awayTeam " +
            "WHERE b.user.id = :userId")
    List<Bet> findByUserId(@Param("userId") UUID userId);
}

