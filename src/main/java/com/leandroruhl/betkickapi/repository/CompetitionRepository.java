package com.leandroruhl.betkickapi.repository;

import com.leandroruhl.betkickapi.model.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The {@code CompetitionRepository} interface extends the {@link JpaRepository} for managing {@link Competition} entities.
 * It provides query methods for retrieving competitions with specific criteria.
 */
@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Integer> {

    /**
     * Retrieves a list of all competitions with scheduled matches.
     *
     * @return A list of {@link Competition} objects with scheduled matches.
     */
    @Query("SELECT DISTINCT c FROM Competition c " +
            "INNER JOIN Match m ON c.id = m.competition.id " +
            "WHERE m.utcDate >= CURRENT_TIMESTAMP")
    List<Competition> findAllCompetitionsWithScheduledMatches();

    /**
     * Retrieves a list of competitions that have standings.
     *
     * @return A list of {@link Competition} objects with standings.
     */
    @Query("SELECT DISTINCT c FROM Competition c " +
            "INNER JOIN CompetitionStandings cs ON cs.competition.id = c.id ")
    List<Competition> findCompetitionsThatHaveStandings();
}

