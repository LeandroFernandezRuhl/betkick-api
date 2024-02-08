package com.example.betkickapi.repository;

import com.example.betkickapi.model.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Integer> {
    @Query("SELECT DISTINCT c FROM Competition c " +
            "INNER JOIN Match m ON c.id = m.competition.id "+
            "WHERE m.utcDate >= CURRENT_TIMESTAMP")
    List<Competition> findAllCompetitionsWithScheduledMatches();
    @Query("SELECT DISTINCT c FROM Competition c " +
            "INNER JOIN CompetitionStandings cs ON cs.competition.id = c.id ")
    List<Competition> findCompetitionsThatHaveStandings();
}
