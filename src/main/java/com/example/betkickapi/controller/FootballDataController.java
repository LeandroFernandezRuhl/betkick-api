package com.example.betkickapi.controller;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.service.competition.CompetitionService;
import com.example.betkickapi.service.match.MatchService;
import com.example.betkickapi.service.standings.StandingsService;
import com.example.betkickapi.service.user.UserService;
import com.example.betkickapi.web.internal.CompetitionStandingsResponse;
import com.example.betkickapi.web.internal.UserBetSummary;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
//@CrossOrigin
@Slf4j
@RequestMapping("/api")
public class FootballDataController {
    private CompetitionService competitionService;
    private MatchService matchService;
    private StandingsService standingsService;
    private UserService userService;

    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserBetSummary>> getLeaderboard() {
        log.info("Request to get leaderboard received");

        return ResponseEntity.ok()
                .body(userService.getUserLeaderboard());
    }

    @GetMapping("/active-competitions")
    public ResponseEntity<List<Competition>> getActiveCompetitions() {
        log.info("Request to get active competitions received");
        return ResponseEntity.ok()
                .body(competitionService.getActiveCompetitions());
    }

    @GetMapping("/competitions-with-standings")
    public ResponseEntity<List<Competition>> getCompetitionsWithStandings() {
        log.info("Request to get with standings competitions received");
        List<Competition> comps = competitionService.getCompetitionsWithStandings();
        return ResponseEntity.ok().body(comps);
    }

    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getMatches() {
        log.info("Request to get matches received");
        return ResponseEntity.ok()
                .body(matchService.getMatches()); // remember to change to today's matches
    }

    @GetMapping(value = "/matches", params = "competitionId")
    public ResponseEntity<List<Match>> getMatchesByCompetitionId(@RequestParam Integer competitionId) {
        log.info("Request to get matches of competition with ID " + competitionId + " received");
        return ResponseEntity.ok()
                .body(matchService.getMatchesByCompetitionId(competitionId));
    }

    @GetMapping(value = "/standings", params = "competitionId")
    public ResponseEntity<List<CompetitionStandingsResponse>> getStandingsByCompetitionId(@RequestParam Integer competitionId) {
        log.info("Request to get standings of competition with ID " + competitionId + " received");

        return ResponseEntity.ok()
                .body(standingsService.getStandingsByCompetitionId(competitionId));
    }
}
