package com.example.betkickapi.repository;

import com.example.betkickapi.model.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The {@code StandingRepository} interface extends the {@link JpaRepository} for managing {@link Standing} entities.
 * It includes custom queries for retrieving standings based on competition and teams, as well as counting standings
 * by competition ID.
 */
@Repository
public interface StandingRepository extends JpaRepository<Standing, Integer> {

    /**
     * Retrieves a list of {@link Standing} objects based on the specified competition and teams.
     * The query fetches associated competition and team entities.
     *
     * @param compId     The ID of the competition.
     * @param homeTeamId The ID of the home team.
     * @param awayTeamId The ID of the away team.
     * @return A list of {@link Standing} objects matching the criteria.
     */
    @Query("SELECT s FROM Standing s " +
            "LEFT JOIN FETCH s.competition " +
            "LEFT JOIN FETCH s.team " +
            "WHERE (s.team.id = :homeTeamId OR s.team.id = :awayTeamId) " +
            "AND s.competition.competition.id = :compId")
    List<Standing> findStandingsByCompAndTeams(@Param("compId") Integer compId, @Param("homeTeamId") Integer homeTeamId,
                                               @Param("awayTeamId") Integer awayTeamId);

    /**
     * Counts the number of standings associated with the specified competition ID.
     *
     * @param compId The ID of the competition.
     * @return The count of standings for the given competition.
     * @see Standing
     */
    @Query("SELECT COUNT(s.id) FROM Standing s WHERE s.competition.id = :compId ")
    Integer countStandingsByCompId(@Param("compId") Integer compId);
}

