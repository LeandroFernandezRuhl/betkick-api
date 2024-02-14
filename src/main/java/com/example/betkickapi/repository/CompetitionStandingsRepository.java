package com.example.betkickapi.repository;

import com.example.betkickapi.model.CompetitionStandings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The {@code CompetitionStandingsRepository} interface extends the {@link JpaRepository} for managing {@link CompetitionStandings} entities.
 * It provides a query method for retrieving competition standings based on the competition ID with eager fetching of associated entities.
 */
@Repository
public interface CompetitionStandingsRepository extends JpaRepository<CompetitionStandings, Integer> {

    /**
     * Retrieves a list of competition standings for a given competition ID with eager fetching of associated entities.
     *
     * @param competitionId The ID of the competition.
     * @return A list of {@link CompetitionStandings} objects with eager fetching.
     */
    @Query("SELECT cs FROM CompetitionStandings cs " +
            "LEFT JOIN FETCH cs.competition " +
            "LEFT JOIN FETCH cs.standings " +
            "WHERE cs.competition.id = :competitionId")
    List<CompetitionStandings> findByCompetitionId(@Param("competitionId") Integer competitionId);
}

