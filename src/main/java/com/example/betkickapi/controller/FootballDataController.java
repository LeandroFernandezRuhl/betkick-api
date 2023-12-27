package com.example.betkickapi.controller;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.CompetitionStandings;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.service.competition.CompetitionService;
import com.example.betkickapi.service.match.MatchService;
import com.example.betkickapi.service.standings.StandingsService;
import com.example.betkickapi.web.internal.CompetitionStandingsResponse;
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

    @GetMapping("/competitions")
    public ResponseEntity<List<Competition>> getCompetitions() {
        log.info("Request to get competitions received");
        return ResponseEntity.ok()
                .body(competitionService.getCompetitions());
    }

    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getTodayMatches() {
        log.info("Request to get today's matches received");
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
