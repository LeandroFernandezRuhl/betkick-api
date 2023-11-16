package com.example.betkickapi.repository;

import com.example.betkickapi.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {
    //List<Match> findByCompetitionId(Integer id);
    // the method above is 3x slower than the one below using join fetch,
    // and also causes N+1, multiplying queries by
    // 1 (fetch matches) + number of matches fetched *  2 (fetch both teams for each match) + 1 (fetch the competition)
    // while the method below just makes one query :)
    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.competition " +
            "LEFT JOIN FETCH m.homeTeam " +
            "LEFT JOIN FETCH m.awayTeam " +
            "WHERE m.competition.id = :competitionId")
    List<Match> findByCompetitionId(@Param("competitionId") Integer competitionId);
}
