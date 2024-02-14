package com.example.betkickapi;

import com.example.betkickapi.dto.external_api.HeadToHeadResponse;
import com.example.betkickapi.dto.external_api.StandingsResponse;
import com.example.betkickapi.dto.external_api.TeamStatsResponse;
import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.Standing;
import com.example.betkickapi.model.embbeded.MatchOdds;
import com.example.betkickapi.service.competition.CompetitionService;
import com.example.betkickapi.service.match.MatchService;
import com.example.betkickapi.service.standings.StandingsService;
import com.example.betkickapi.service.utility.CacheService;
import com.example.betkickapi.service.utility.FootballApiService;
import com.example.betkickapi.service.utility.OddsCalculationService;
import jakarta.transaction.Transactional;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Scheduled job responsible for various tasks related to football match data and odds calculation.
 * It utilizes various services and APIs to fetch, calculate, and update data at specified intervals.
 * <br>
 * <br>
 * Tasks schedule:
 * <br>
 * * Every 65 seconds (conditional) - {@code scheduledOddsCalculation}.
 * <br>
 * * Every 62 seconds (conditional) - {@code updateMatches}.
 * <br>
 * * Every 12 hours - {@code checkMatchesToday}: Determines if {@code updateMatches} is allowed to execute.
 * <br>
 * * Daily at 23:58:00 - {@code startMidnightTasks}: Sets up a flag that stops {@code scheduledOddsCalculation} and {@code updateMatches} from executing.
 * <br>
 * * Daily at 00:00:00 - {@code saveUpcomingMatches}: Fetches and saves upcoming matches for the next ~3 months, updating those that are already stored.
 * <br>
 * * Daily at 00:01:10 - {@code updateFirstHalfStandings}.
 * <br>
 * * Daily at 00:02:10 - {@code updateSecondHalfStandings}.
 * <br>
 * * Daily at 00:03:50 - {@code endMidnightTasks}: Resets the flag that stops {@code scheduledOddsCalculation} and {@code updateMatches} from executing.
 */

@Component
@Slf4j
public class JobScheduler {
    private FootballApiService footballApiService;
    private MatchService matchService;
    private OddsCalculationService oddsService;
    private CompetitionService competitionService;
    private StandingsService standingsService;
    private CacheService cacheService;
    private Boolean matchesToday;
    @Setter
    private Boolean shouldCalculateMatchOdds;
    @Setter
    private Boolean secondaryTasksCanExecute;
    private List<StandingsResponse> standingsList;

    public JobScheduler(FootballApiService footballApiService, CompetitionService competitionService, CacheService cacheService,
                        MatchService matchService, OddsCalculationService oddsService, StandingsService standingsService) {
        this.footballApiService = footballApiService;
        this.competitionService = competitionService;
        this.matchService = matchService;
        this.cacheService = cacheService;
        this.oddsService = oddsService;
        this.standingsService = standingsService;
        this.matchesToday = false;
        this.shouldCalculateMatchOdds = false;
        this.secondaryTasksCanExecute = false;
        this.standingsList = new ArrayList<>();
    }

    /**
     * Scheduled task to calculate real match odds for those matches that have random (placeholder) odds.
     */
    @Scheduled(fixedDelay = 65000) // Every 65 seconds
    @Transactional
    public void scheduledOddsCalculation() {
        if (shouldCalculateMatchOdds && secondaryTasksCanExecute) {
            // Gets at most 3 matches, which is the maximum number of matches that can be calculated per minute
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

    /**
     * Calculates match odds for a given match using external API data and statistics.
     * The method is invoked by the {@code scheduledOddsCalculation} task.
     *
     * @param match The football match for which odds are to be calculated.
     */
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


        List<Standing> standings = standingsService.getStandingsByCompIdAndTeams(match.getCompetition().getId(), homeId, awayId);
        Integer totalTeams = 0;
        if (!standings.isEmpty())
            totalTeams = standingsService.countStandingsByCompId(standings.getFirst().getCompetition().getId());
        else {
            standings = null;
        }

        log.info("CALCULATING ODDS FOR " + match.getHomeTeam().getShortName() + " (HOME) VS (AWAY) " + match.getAwayTeam().getShortName());
        log.info("Match ID: " + match.getId());

        // Calculate odds using fetched statistics and head-to-head data
        MatchOdds calculatedOdds = oddsService.generateMatchOdds(homeStats, awayStats, headToHead, standings, homeId, totalTeams);

        if (calculatedOdds != null) {
            match.setOdds(calculatedOdds);
            matchService.updateMatch(match);
        } else {
            log.warn("Odds for this match can't be properly calculated, so it will keep its default random odds");
            match.getOdds().setTemporaryRandomOdds(false); // the application won't try to calculate this match odds again
        }
    }

    /**
     * A game's score, status and duration can change any second. This scheduled task
     * updates all the matches of the current day with the latest data (status, score, etc.).
     */
    @Scheduled(fixedDelay = 62000) // Every 62 seconds
    @Transactional
    public void updateMatches() {
        // 62 seconds to account for communication latency between this and the external API clocks
        if (matchesToday && secondaryTasksCanExecute) {
            log.info("Scheduled task to update matches is being executed");
            footballApiService.fetchAndUpdateMatches();
        }
    }

    /**
     * Scheduled task to check if there are matches today.
     */
    @Scheduled(cron = "0 0 */12 * * *") // Cron expression for every 12 hours
    public void checkMatchesToday() {
        this.matchesToday = matchService.areThereMatchesToday();
    }

    /**
     * Scheduled task to start midnight tasks, setting a flag that stops other methods from executing
     */
    @Scheduled(cron = "0 58 23 * * *") // Cron expression for 23:58:00
    public void startMidnightTasks() {
        log.warn("MIDNIGHT TASKS STARTING");
        this.secondaryTasksCanExecute = false;
    }

    /**
     * Scheduled task to end midnight tasks.
     */
    @Scheduled(cron = "50 3 0 * * *")
    public void endMidnightTasks() {
        log.warn("MIDNIGHT TASKS COMPLETED");
        this.secondaryTasksCanExecute = true;
    }

    /**
     * Scheduled task to save upcoming matches and trigger odds calculation.
     */
    @Scheduled(cron = "0 0 0 * * *") // Cron expression for midnight (00:00:00) every day
    @Transactional
    public void saveUpcomingMatches() {
        // Get this month's matches, has to be done in 10 days intervals because of API restriction
        Instant currentInstant = Instant.now();
        LocalDate currentDate = LocalDate.ofInstant(currentInstant, ZoneOffset.UTC);
        int from = 0;
        int to = 10;
        // Get matches from today to approx 3 months in the future
        for (int i = 0; i < 9; i++) {
            footballApiService.fetchAndSaveMatches(
                    currentDate.plusDays(from),
                    currentDate.plusDays(to),
                    false);
            from += 10;
            to += 10;
        }
        shouldCalculateMatchOdds = true;
        cacheService.invalidateCacheForKey("activeCompetitions");
    }

    /**
     * Scheduled task to update standings for the first half of competitions.
     * <br>
     * <br>
     * Standings need to be updated in two batches because the number of requests
     * exceeds the maximum per minute.
     */
    @Scheduled(cron = "10 1 0 * * *") // Cron expression for 00:01:10
    public void updateFirstHalfStandings() {
        List<Competition> competitions = competitionService.getAllCompetitions();
        for (int i = 0; i < 6; i++) {
            this.standingsList.add(footballApiService.fetchStandings(competitions.get(i)));
        }
    }

    /**
     * Scheduled task to update standings for the second half of competitions.
     */
    @Scheduled(cron = "10 2 0 * * *") // Cron expression for 00:02:10
    @Transactional
    public void updateSecondHalfStandings() {
        List<Competition> competitions = competitionService.getAllCompetitions();

        for (int i = 6; i < 12; i++) {
            this.standingsList.add(footballApiService.fetchStandings(competitions.get(i)));
        }

        standingsService.deleteStandings();
        footballApiService.saveStandings(standingsList);

        this.standingsList.clear();
        cacheService.invalidateCacheForKey("competitionsWithStandings");
    }
}
