package com.example.betkickapi.controller;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.service.competition.CompetitionService;
import com.example.betkickapi.service.match.MatchService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api")
public class FootballDataController {
    private CompetitionService competitionService;
    private MatchService matchService;

    @GetMapping("/competitions")
    public ResponseEntity<List<Competition>> getCompetitions() {
        return ResponseEntity.ok()
                .body(competitionService.getCompetitions());
    }

    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getTodayMatches() {
        return ResponseEntity.ok()
                .body(matchService.getMatches()); // remember to change to today's matches
    }

    @GetMapping(value = "/matches", params = "competitionId")
    public ResponseEntity<List<Match>> getMatchesByCompetitionId(@RequestParam Integer competitionId) {
        return ResponseEntity.ok()
                .body(matchService.getByCompetitionId(competitionId));
    }
}
