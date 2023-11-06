package com.example.betkickapi.controller;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.service.FootballDataService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api")
public class FootballDataController {
    private FootballDataService footballDataService;

    @GetMapping("/competitions")
    public ResponseEntity<List<Competition>> getCompetitions() {
        return ResponseEntity.ok()
                .body(footballDataService.getCompetitionsFromApi());
    }

    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getTodayMatches() {
        return ResponseEntity.ok()
                .body(footballDataService.getTodayMatchesFromApi());
    }
}
