package com.example.betkickapi;

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
import com.example.betkickapi.web.externalApi.HeadToHeadResponse;
import com.example.betkickapi.web.externalApi.StandingsResponse;
import com.example.betkickapi.web.externalApi.TeamStatsResponse;
import jakarta.transaction.Transactional;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    @Scheduled(fixedDelay = 65000)
    @Transactional
    public void scheduledOddsCalculation() {
        if (shouldCalculateMatchOdds && secondaryTasksCanExecute) {
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

    // a game's score, status and duration can change any second
    // but polling the Football Data Org API every second is not feasible
    // so the games of the current day are checked every minute
    @Scheduled(fixedDelay = 62000)
    @Transactional
    public void updateMatches() {
        // 62 seconds to account for communication latency between this and the external API clocks
        if (matchesToday && secondaryTasksCanExecute) {
            log.info("Scheduled task to update matches is being executed");
            footballApiService.fetchAndUpdateMatches();
        }
    }

    @Scheduled(cron = "0 0 */12 * * *") // Cron expression for every 12 hours
    public void checkMatchesToday() {
        this.matchesToday = matchService.areThereMatchesToday();
    }

    @Scheduled(cron = "0 58 23 * * *")
    public void startMidnightTasks() {
        log.warn("MIDNIGHT TASKS STARTING");
        this.secondaryTasksCanExecute = false;
    }

    @Scheduled(cron = "50 3 0 * * *")
    public void endMidnightTasks() {
        log.warn("MIDNIGHT TASKS COMPLETED");
        this.secondaryTasksCanExecute = true;
    }

    // a game's status and programmed date can change anytime, so it needs to be checked daily
    @Scheduled(cron = "0 0 0 * * *") // Cron expression for midnight (00:00:00) every day
    @Transactional
    public void saveUpcomingMatches() {
        // get this month's matches, has to be done in 10 days intervals because of API restriction
        Instant currentInstant = Instant.now();
        LocalDate currentDate = LocalDate.ofInstant(currentInstant, ZoneOffset.UTC);
        int from = 0;
        int to = 10;
        // get matches from today to approx 3 months in the future
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

    @Scheduled(cron = "10 1 0 * * *")
    public void updateFirstHalfStandings() {
        List<Competition> competitions = competitionService.getAllCompetitions();
        for (int i = 0; i < 6; i++) {
            this.standingsList.add(footballApiService.fetchStandings(competitions.get(i)));
        }
    }

    // standings need to be updated separately because the number of requests
    // exceeds the maximum, so a one-minute wait is needed
    @Scheduled(cron = "10 2 0 * * *")
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
