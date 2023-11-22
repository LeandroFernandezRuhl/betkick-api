package com.example.betkickapi.service;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.Score;
import com.example.betkickapi.model.Team;
import com.example.betkickapi.response.CompetitionsResponse;
import com.example.betkickapi.response.MatchesResponse;
import com.example.betkickapi.service.competition.CompetitionService;
import com.example.betkickapi.service.match.MatchService;
import com.example.betkickapi.service.team.TeamService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FootballApiService {
    private RestTemplate restTemplate;
    private final String API_KEY;
    private CompetitionService competitionService;
    private MatchService matchService;
    private TeamService teamService;

    @Autowired
    public FootballApiService(RestTemplate restTemplate, Environment env,
                              CompetitionService competitionService, MatchService matchService, TeamService teamService) {
        this.API_KEY = env.getProperty("API_KEY");
        this.restTemplate = restTemplate;
        this.competitionService = competitionService;
        this.matchService = matchService;
        this.teamService = teamService;
    }

    public void fetchAndSaveCompetitions() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<CompetitionsResponse> response = restTemplate.exchange(
                "https://api.football-data.org/v4/competitions",
                HttpMethod.GET,
                entity,
                CompetitionsResponse.class
        );

        List<Competition> competitions = response.getBody().getCompetitions();
        competitionService.saveCompetitions(competitions);
    }

    @Transactional
    public void fetchAndSaveMatches(LocalDate dateFrom, LocalDate dateTo, Boolean saveOrUpdate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<MatchesResponse> response = restTemplate.exchange(
                "https://api.football-data.org/v4/matches?dateFrom={dateFrom}&dateTo={dateTo}",
                HttpMethod.GET,
                entity,
                MatchesResponse.class,
                dateFrom,
                dateTo
        );

        this.saveMatches(response.getBody().getMatches(), saveOrUpdate);
    }


    @Transactional
    public void fetchAndUpdateMatches() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<MatchesResponse> response = restTemplate.exchange(
                "https://api.football-data.org/v4/matches",
                HttpMethod.GET,
                entity,
                MatchesResponse.class
        );

        List<Match> matches = response.getBody().getMatches();
        if (matches.size() > 0) {
            matches.forEach(match -> match.setNew(false)); // entities are guaranteed to already be in the DB
            matchService.updateMatches(matches);
        }
    }


    private void saveMatches(List<Match> matches, Boolean saveOrUpdate) {
        Set<Team> teams = matches.stream()
                .flatMap(match -> Stream.of(match.getHomeTeam(), match.getAwayTeam()))
                .collect(Collectors.toSet());

        teamService.saveTeams(teams);

        matches.forEach(match -> {
            // because of the API's design all teams and competitions
            // are guaranteed to already exist in DB, so this gets
            // proxy objects to avoid unnecessary selects that check for
            // the entities existence
            Integer homeTeamId = match.getHomeTeam().getId();
            Integer awayTeamId = match.getAwayTeam().getId();
            Integer competitionId = match.getCompetition().getId();
            match.setHomeTeam(teamService.getReference(homeTeamId));
            match.setAwayTeam(teamService.getReference(awayTeamId));
            match.setCompetition(competitionService.getReference(competitionId));
        });

        if (saveOrUpdate)
            matchService.saveOrUpdateMatches(matches);
        else
            matchService.saveMatches(matches);
    }

    @Transactional
    public void updateMatchesTest(LocalDate dateFrom, LocalDate dateTo) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<MatchesResponse> response = restTemplate.exchange(
                "https://api.football-data.org/v4/matches?dateFrom={dateFrom}&dateTo={dateTo}",
                HttpMethod.GET,
                entity,
                MatchesResponse.class,
                dateFrom,
                dateTo
        );

        List<Match> matches = response.getBody().getMatches();

        matches.forEach(match -> {
            match.setScore(new Score(0, 1, 20, 1));
            match.setNew(false);
        }); // entities are guaranteed to exist in the DB

        matchService.updateMatches(matches);
    }
}