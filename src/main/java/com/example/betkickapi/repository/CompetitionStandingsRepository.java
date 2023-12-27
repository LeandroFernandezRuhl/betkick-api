package com.example.betkickapi.repository;

import com.example.betkickapi.model.CompetitionStandings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionStandingsRepository extends JpaRepository<CompetitionStandings, Integer> {
    @Query("SELECT cs FROM CompetitionStandings cs " +
            "LEFT JOIN FETCH cs.competition " +
            "LEFT JOIN FETCH cs.standings " +
            "WHERE cs.competition.id = :competitionId")
    List<CompetitionStandings> findByCompetitionId(@Param("competitionId") Integer competitionId);
}
