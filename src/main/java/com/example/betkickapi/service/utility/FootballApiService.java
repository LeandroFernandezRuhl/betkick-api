package com.example.betkickapi.service.utility;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.Team;
import com.example.betkickapi.model.enums.Status;
import com.example.betkickapi.model.enums.Winner;
import com.example.betkickapi.model.embbeded.Score;
import com.example.betkickapi.response.TeamStatsResponse;
import com.example.betkickapi.response.CompetitionsResponse;
import com.example.betkickapi.response.HeadToHeadResponse;
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

    // fetches the number of wins, draws and loses of a team
    public TeamStatsResponse fetchTeamStats(Integer teamId, LocalDate dateFrom, LocalDate dateTo) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // stats extracted from "resultSet" object in the JSON response
        ResponseEntity<TeamStatsResponse> response = restTemplate.exchange(
                "https://api.football-data.org/v4/teams/{teamId}/matches?dateFrom={dateFrom}&dateTo={dateTo}&limit=200",
                HttpMethod.GET,
                entity,
                TeamStatsResponse.class,
                teamId,
                dateFrom,
                dateTo
        );

        return response.getBody();
    }

    public HeadToHeadResponse fetchHeadToHead(Integer matchId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<HeadToHeadResponse> response = restTemplate.exchange(
                "https://api.football-data.org/v4/matches/{matchId}/head2head?limit=100",
                HttpMethod.GET,
                entity,
                HeadToHeadResponse.class,
                matchId
        );

        return response.getBody();
    }

    public List<Competition> fetchCompetitions() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<CompetitionsResponse> response = restTemplate.exchange(
                "https://api.football-data.org/v4/competitions",
                HttpMethod.GET,
                entity,
                CompetitionsResponse.class
        );

        return response.getBody().getCompetitions();
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

        // this only gets today's games
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
    public void fetchAndUpdateMatchesTest() {
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
            System.out.println("MATCH TO TEST: ");
            System.out.println(matches.get(1));
            matches.get(1).setStatus(Status.FINISHED);
            matches.get(1).setWinner(Winner.DRAW);
            matches.forEach(match -> match.setNew(false)); // entities are guaranteed to already be in the DB
            matchService.updateMatches(matches);
        }
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