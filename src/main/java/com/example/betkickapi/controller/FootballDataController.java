package com.example.betkickapi.controller;

import com.example.betkickapi.dto.internal_api.CompetitionStandingsResponse;
import com.example.betkickapi.dto.internal_api.UserBetSummary;
import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.service.competition.CompetitionService;
import com.example.betkickapi.service.match.MatchService;
import com.example.betkickapi.service.standings.StandingsService;
import com.example.betkickapi.service.user.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The FootballDataController class is a Spring Web MVC controller responsible for handling football data-related
 * endpoints, including leaderboard, active competitions, competitions with standings, matches, and standings by competition ID.
 */
@AllArgsConstructor
@RestController
//@CrossOrigin
@Slf4j
@RequestMapping("/api")
public class FootballDataController {

    private final CompetitionService competitionService;
    private final MatchService matchService;
    private final StandingsService standingsService;
    private final UserService userService;

    /**
     * Retrieves the leaderboard containing user summaries based on various criteria such as win rate, total number of won bets,
     * and net profit.
     *
     * @return A {@link ResponseEntity} containing a list of {@link UserBetSummary}.
     * @see UserService#getUserLeaderboard()
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserBetSummary>> getLeaderboard() {
        log.info("Request to get leaderboard received");

        return ResponseEntity.ok()
                .body(userService.getUserLeaderboard());
    }

    /**
     * Retrieves a list of active competitions.
     *
     * @return A {@link ResponseEntity} containing a list of {@link Competition}.
     * @see CompetitionService#getActiveCompetitions()
     */
    @GetMapping("/active-competitions")
    public ResponseEntity<List<Competition>> getActiveCompetitions() {
        log.info("Request to get active competitions received");
        return ResponseEntity.ok()
                .body(competitionService.getActiveCompetitions());
    }

    /**
     * Retrieves a list of competitions with standings.
     *
     * @return A {@link ResponseEntity} containing a list of {@link Competition}.
     * @see CompetitionService#getCompetitionsWithStandings()
     */
    @GetMapping("/competitions-with-standings")
    public ResponseEntity<List<Competition>> getCompetitionsWithStandings() {
        log.info("Request to get competitions with standings received");
        List<Competition> comps = competitionService.getCompetitionsWithStandings();
        return ResponseEntity.ok().body(comps);
    }

    /**
     * Retrieves all non-finished (live, upcoming, etc.) matches.
     *
     * @return A {@link ResponseEntity} containing a list of {@link Match}.
     * @see MatchService#getNonFinishedMatches()
     */
    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getNonFinishedMatches() {
        log.info("Request to get matches received");
        return ResponseEntity.ok()
                .body(matchService.getNonFinishedMatches());
    }

    /**
     * Retrieves a list of matches for a specific competition ID.
     *
     * @param competitionId The ID of the competition.
     * @return A {@link ResponseEntity} containing a list of {@link Match}.
     * @see MatchService#getMatchesByCompetitionId(Integer)
     */
    @GetMapping(value = "/matches", params = "competitionId")
    public ResponseEntity<List<Match>> getMatchesByCompetitionId(@RequestParam Integer competitionId) {
        log.info("Request to get matches of competition with ID " + competitionId + " received");
        return ResponseEntity.ok()
                .body(matchService.getMatchesByCompetitionId(competitionId));
    }

    /**
     * Retrieves standings for a specific competition ID.
     *
     * @param competitionId The ID of the competition.
     * @return A {@link ResponseEntity} containing a list of {@link CompetitionStandingsResponse}.
     * @see StandingsService#getStandingsByCompetitionId(Integer)
     */
    @GetMapping(value = "/standings", params = "competitionId")
    public ResponseEntity<List<CompetitionStandingsResponse>> getStandingsByCompetitionId(@RequestParam Integer competitionId) {
        log.info("Request to get standings of competition with ID " + competitionId + " received");

        return ResponseEntity.ok()
                .body(standingsService.getStandingsByCompetitionId(competitionId));
    }
}
