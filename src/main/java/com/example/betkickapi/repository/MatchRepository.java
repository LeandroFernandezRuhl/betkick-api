package com.example.betkickapi.repository;

import com.example.betkickapi.model.Match;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * The MatchRepository interface extends the {@link JpaRepository} for managing {@link Match} entities.
 * It provides various query methods for retrieving matches based on different criteria.
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {

    /**
     * Retrieves a list of matches for a given competition ID with eager fetching of associated entities.
     *
     * @param competitionId The ID of the competition.
     * @return A list of {@link Match} objects with eager fetching.
     */
    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.competition " +
            "LEFT JOIN FETCH m.homeTeam " +
            "LEFT JOIN FETCH m.awayTeam " +
            "WHERE m.competition.id = :competitionId")
    List<Match> findByCompetitionId(@Param("competitionId") Integer competitionId);

    /**
     * Retrieves a list of matches with temporary random odds set to true and paginated results.
     *
     * @param pageable The pagination information.
     * @return A paginated list of {@link Match} objects with eager fetching.
     */
    List<Match> findByOdds_TemporaryRandomOddsIsTrue(Pageable pageable);

    /**
     * Retrieves a list of matches for given IDs with eager fetching and excluding finished or awarded matches.
     *
     * @param ids The list of match IDs.
     * @return A list of {@link Match} objects with eager fetching.
     */
    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.competition " +
            "LEFT JOIN FETCH m.homeTeam " +
            "LEFT JOIN FETCH m.awayTeam " +
            "WHERE m.id IN :ids AND m.status <> 'FINISHED' AND m.status <> 'AWARDED'")
    List<Match> findByIdsAndStatusIsNotFinished(@Param("ids") List<Integer> ids);

    /**
     * Retrieves a list of all unfinished matches with eager fetching.
     *
     * @return A list of {@link Match} objects with eager fetching.
     */
    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.competition " +
            "LEFT JOIN FETCH m.homeTeam " +
            "LEFT JOIN FETCH m.awayTeam " +
            "WHERE m.status <> 'AWARDED' AND m.status <> 'FINISHED' AND m.winner IS NULL")
    List<Match> findAllUnfinishedMatches();

    /**
     * Retrieves existing match IDs from a given list.
     *
     * @param ids The list of match IDs.
     * @return A list of existing match IDs.
     */
    @Query("SELECT m.id FROM Match m WHERE m.id IN :ids")
    List<Integer> findExistingMatchIds(@Param("ids") List<Integer> ids);

    /**
     * Retrieves a list of matches for given IDs with eager fetching.
     *
     * @param ids The list of match IDs.
     * @return A list of {@link Match} objects with eager fetching.
     */
    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.competition " +
            "LEFT JOIN FETCH m.homeTeam " +
            "LEFT JOIN FETCH m.awayTeam " +
            "WHERE m.id IN :ids")
    List<Match> findMatchesByIds(@Param("ids") List<Integer> ids);

    /**
     * Checks if matches exist within a specified date range.
     *
     * @param from The start date.
     * @param to   The end date.
     * @return {@code true} if matches exist within the date range; {@code false} otherwise.
     */
    @Query("SELECT EXISTS (SELECT 1 FROM Match m WHERE DATE(m.utcDate) BETWEEN :from AND :to)")
    Boolean existsByUtcDate(@Param("from") LocalDate from, @Param("to") LocalDate to);
}

