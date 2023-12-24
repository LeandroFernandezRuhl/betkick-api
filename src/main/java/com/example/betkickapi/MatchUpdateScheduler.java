package com.example.betkickapi;

import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.embbeded.MatchOdds;
import com.example.betkickapi.response.TeamStatsResponse;
import com.example.betkickapi.response.HeadToHeadResponse;
import com.example.betkickapi.service.utility.FootballApiService;
import com.example.betkickapi.service.utility.OddsCalculationService;
import com.example.betkickapi.service.match.MatchService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
@Slf4j
public class MatchUpdateScheduler {
    private FootballApiService footballApiService;
    private MatchService matchService;
    private OddsCalculationService oddsService;
    private Boolean matchesToday;
    private Boolean shouldCalculateMatchOdds;
    private Boolean midnightTaskExecuting;
    private Boolean isFirstRequest;

    public MatchUpdateScheduler(FootballApiService footballApiService,
                                MatchService matchService, OddsCalculationService oddsService) {
        this.footballApiService = footballApiService;
        this.matchService = matchService;
        this.oddsService = oddsService;
        this.matchesToday = false;
        this.shouldCalculateMatchOdds = false;
        this.midnightTaskExecuting = false;
        this.isFirstRequest = true;
    }

    public void setShouldCalculateMatchOdds(Boolean shouldCalculateMatchOdds) {
        this.shouldCalculateMatchOdds = shouldCalculateMatchOdds;
    }

    @Scheduled(fixedDelay = 65000)
    @Transactional
    public void scheduledOddsCalculation() {
        if (shouldCalculateMatchOdds && !midnightTaskExecuting) {
            List<Match> matches = matchService.findMatchesWithRandomOdds();
            if (matches.isEmpty()) {
                log.info("NO MATCHES WITH RANDOM ODDS FOUND");
                shouldCalculateMatchOdds = false;
            } else {
                log.info("THERE ARE " + matches.size() + " MATCHES WITH RANDOM ODDS");
                matches.forEach(this::calculateMatchOdds);
            }
        }
    }

    private void calculateMatchOdds(Match match) {
        // Get the current date in UTC
        Instant currentInstant = Instant.now();
        LocalDate currentDate = LocalDate.ofInstant(currentInstant, ZoneOffset.UTC);

        // Fetch statistics for the away and home teams from external API
        Integer awayId = match.getAwayTeam().getId();
        Integer homeId = match.getHomeTeam().getId();

        // Fetch and unpack statistics for the away team
        TeamStatsResponse awayStats = footballApiService.fetchTeamStats(awayId, currentDate.minusYears(2), currentDate);
        awayStats.unpackNestedMatches(awayId);

        // Fetch and unpack statistics for the home team
        TeamStatsResponse homeStats = footballApiService.fetchTeamStats(homeId, currentDate.minusYears(2), currentDate);
        homeStats.unpackNestedMatches(homeId);

        // Fetch head-to-head statistics for the given match
        HeadToHeadResponse headToHead = footballApiService.fetchHeadToHead(match.getId());

        // Process and unpack head-to-head statistics for the away team
        TeamStatsResponse h2hAwayStats = headToHead.getAwayTeamStats();
        if (h2hAwayStats != null) {
            h2hAwayStats.setMatches(headToHead.getMatches());
            h2hAwayStats.unpackNestedMatches(awayId);
        }

        // Process and unpack head-to-head statistics for the home team
        TeamStatsResponse h2hHomeStats = headToHead.getHomeTeamStats();
        if (h2hHomeStats != null) {
            h2hHomeStats.setMatches(headToHead.getMatches());
            h2hHomeStats.unpackNestedMatches(homeId);
        }

        log.info("CALCULATING ODDS FOR " + match.getHomeTeam().getShortName() + " (HOME) VS (AWAY) " + match.getAwayTeam().getShortName());
        log.info("Match ID: " + match.getId());

        // Calculate odds using fetched statistics and head-to-head data
        MatchOdds calculatedOdds = oddsService.generateMatchOdds(homeStats, awayStats, headToHead);

        if (calculatedOdds != null) {
            match.setOdds(calculatedOdds);
            matchService.updateMatch(match);
        } else {
            log.warn("Odds for this match can't be properly calculated, so it will keep its default random odds");
            match.getOdds().setTemporaryRandomOdds(false); // the application won't try to calculate this match odds again
        }
    }

    // a game's score, status and duration can change any second
    // but polling the Football Data Org API every second is not feasible
    // so the games of the current day are checked every minute
    @Scheduled(fixedDelay = 62000)
    @Transactional
    public void updateMatches() {
        // 65 seconds to account for communication latency between this and the external API clocks
        if ((isFirstRequest && matchesToday) || (matchesToday && !midnightTaskExecuting)) {
            log.info("Scheduled task to update matches is being executed");
            footballApiService.fetchAndUpdateMatches();
            isFirstRequest = false;
        }
    }

    // modify to execute every 12 hours
    @Scheduled(cron = "0 0 0 * * ?") // Cron expression for midnight (00:00:00) every day
    public void checkMatchesToday() {
        this.matchesToday = matchService.areThereMatchesToday();
    }

    @Scheduled(cron = "0 58 23 * * *")
    public void startMidnightTask() {
        midnightTaskExecuting = true;
    }

    @Scheduled(cron = "0 2 0 * * *")
    public void endMidnightTask() {
        midnightTaskExecuting = false;
    }

    // a game's status and programmed date can change anytime, so it needs to be checked daily
    @Scheduled(cron = "0 0 0 * * *") // Cron expression for midnight (00:00:00) every day
    @Transactional
    public void saveUpcomingMatches() {
        // get this month's matches, has to be done in 10 days intervals because of API restriction
        Instant currentInstant = Instant.now();
        LocalDate currentDate = LocalDate.ofInstant(currentInstant, ZoneOffset.UTC);
        footballApiService.fetchAndSaveMatches(
                currentDate,
                currentDate.plusDays(10),
                true);
        footballApiService.fetchAndSaveMatches(
                currentDate.plusDays(10),
                currentDate.plusDays(20),
                true);
        footballApiService.fetchAndSaveMatches(
                currentDate.plusDays(20),
                currentDate.plusDays(30),
                true);
        shouldCalculateMatchOdds = true;
    }
}
