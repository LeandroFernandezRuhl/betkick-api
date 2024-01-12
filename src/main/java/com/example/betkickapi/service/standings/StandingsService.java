package com.example.betkickapi.service.standings;

import com.example.betkickapi.model.CompetitionStandings;
import com.example.betkickapi.model.Standing;
import com.example.betkickapi.web.internal.CompetitionStandingsResponse;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface StandingsService {
    @Cacheable(value = "standingsCache", key = "'standingsByCompetitionId-' + #id")
    // beware of what's cached when this method is called but the database is empty (it caches an empty array)
    // also remember to invalid cache according to the cron update
    List<CompetitionStandingsResponse> getStandingsByCompetitionId(Integer competitionId);

    void saveStandings(List<CompetitionStandings> standings);

    @Transactional
    void deleteStandings();

    List<Standing> getStandingsByCompIdAndTeams(Integer compId, Integer homeTeamId, Integer awayTeamId);

    Integer countStandingsByCompId(Integer compId);
}
