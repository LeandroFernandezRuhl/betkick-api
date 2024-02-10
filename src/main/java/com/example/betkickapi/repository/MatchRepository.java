package com.example.betkickapi.repository;

import com.example.betkickapi.model.Match;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {
    //List<Match> findByCompetitionId(Integer id);
    // the method above is ~3x slower than the one below using join fetch,
    // and also causes N+1, multiplying queries by
    // 1 (fetch matches) + number of matches fetched *  2 (fetch both teams for each match) + 1 (fetch the competition)
    // while the method below just makes one query :)
    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.competition " +
            "LEFT JOIN FETCH m.homeTeam " +
            "LEFT JOIN FETCH m.awayTeam " +
            "WHERE m.competition.id = :competitionId")
    List<Match> findByCompetitionId(@Param("competitionId") Integer competitionId);

    // can't use LIMIT so pageable is necessary
    // also using native queries gives NonUniqueDiscoveredSqlAliasException when doing the joins
    // and when using JPQL the fields of the odds embedded object are not accessible
    // so, given that only 3 entities are returned (page size), the best option is to use a query method instead of SQL
    // because trying to workaround all those issues above just to make a few less selects is overkill,
    // since the number of queries is already minimal
    List<Match> findByOdds_TemporaryRandomOddsIsTrue(Pageable pageable);

    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.competition " +
            "LEFT JOIN FETCH m.homeTeam " +
            "LEFT JOIN FETCH m.awayTeam " +
            "WHERE m.id IN :ids AND m.status <> 'FINISHED' AND m.status <> 'AWARDED'")
    List<Match> findByIdsAndStatusIsNotFinished(List<Integer> ids);

    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.competition " +
            "LEFT JOIN FETCH m.homeTeam " +
            "LEFT JOIN FETCH m.awayTeam " +
            "WHERE m.status <> 'AWARDED' AND m.status <> 'FINISHED' AND m.winner IS NULL")
    List<Match> findAllUnfinishedMatches();

    @Query("SELECT m.id FROM Match m WHERE m.id IN :ids")
    List<Integer> findExistingMatchIds(@Param("ids") List<Integer> ids);

    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.competition " +
            "LEFT JOIN FETCH m.homeTeam " +
            "LEFT JOIN FETCH m.awayTeam " +
            "WHERE m.id IN :ids")
    List<Match> findMatchesByIds(@Param("ids") List<Integer> ids);

    @Query("SELECT EXISTS (SELECT 1 FROM Match m WHERE DATE(m.utcDate) BETWEEN :from AND :to)")
    Boolean existsByUtcDate(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
