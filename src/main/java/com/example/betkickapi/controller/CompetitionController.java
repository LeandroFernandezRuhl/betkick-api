package com.example.betkickapi.controller;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.service.CompetitionService;
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
public class CompetitionController {
    private CompetitionService competitionService;

    @GetMapping("/competitions")
    public ResponseEntity<List<Competition>> getCompetitions() {
        return ResponseEntity.ok()
                .body(competitionService.getCompetitionsFromApi());
    }
}
