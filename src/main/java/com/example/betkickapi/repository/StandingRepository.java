package com.example.betkickapi.repository;

import com.example.betkickapi.model.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StandingRepository extends JpaRepository<Standing, Integer> {
    @Query("SELECT s FROM Standing s " +
            "LEFT JOIN FETCH s.competition " +
            "LEFT JOIN FETCH s.team " +
            "WHERE (s.team.id = :homeTeamId OR s.team.id = :awayTeamId) " +
            "AND s.competition.competition.id = :compId")
    List<Standing> findStandingsByCompAndTeams(@Param("compId") Integer compId, @Param("homeTeamId") Integer homeTeamId,
                                               @Param("awayTeamId") Integer awayTeamId);

    @Query("SELECT COUNT(s.id) FROM Standing s " +
            "WHERE s.competition.id = :compId ")
    Integer countStandingsByCompId(@Param("compId") Integer compId);
}
