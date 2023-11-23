package com.example.betkickapi.repository;

import com.example.betkickapi.model.Bet;
import com.example.betkickapi.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, Integer> {
    @Query("SELECT b FROM Bet b " +
            "LEFT JOIN FETCH b.user " +
            "WHERE b.match.id = :matchId")
    List<Bet> findByMatchId(@Param("matchId") Integer matchId);

    @Query("SELECT b FROM Bet b " +
            "LEFT JOIN FETCH b.match " +
            "LEFT JOIN FETCH b.match.competition " +
            "LEFT JOIN FETCH b.match.homeTeam " +
            "LEFT JOIN FETCH b.match.awayTeam " +
            "WHERE b.user.id = :userId")
    List<Bet> findByUserId(@Param("userId") String userId);
}
