package com.leandroruhl.betkickapi.repository;

import com.leandroruhl.betkickapi.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * The {@code TeamRepository} interface extends the {@link JpaRepository} for managing {@link Team} entities.
 * It provides a custom query for retrieving all teams as a set.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

    /**
     * Retrieves all teams from the database as a {@link Set}.
     *
     * @return A {@link Set} of {@link Team} objects representing all teams.
     */
    @Query(value = "SELECT * FROM team", nativeQuery = true)
    Set<Team> findAllTeams();
}

