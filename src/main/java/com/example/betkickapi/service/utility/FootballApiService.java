package com.example.betkickapi.service.utility;

import com.example.betkickapi.model.*;
import com.example.betkickapi.service.competition.CompetitionService;
import com.example.betkickapi.service.match.MatchService;
import com.example.betkickapi.service.standings.StandingsService;
import com.example.betkickapi.service.team.TeamService;
import com.example.betkickapi.web.externalApi.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class FootballApiService {
    private RestTemplate restTemplate;
    private final String API_KEY;
    private CompetitionService competitionService;
    private MatchService matchService;
    private TeamService teamService;
    private StandingsService standingsService;

    @Autowired
    public FootballApiService(RestTemplate restTemplate, Environment env, StandingsService standingsService,
                              CompetitionService competitionService, MatchService matchService, TeamService teamService) {
        this.API_KEY = env.getProperty("API_KEY");
        this.restTemplate = restTemplate;
        this.competitionService = competitionService;
        this.matchService = matchService;
        this.teamService = teamService;
        this.standingsService = standingsService;
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

    public StandingsResponse fetchStandings(Competition competition) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<StandingsResponse> response = restTemplate.exchange(
                "https://api.football-data.org/v4/competitions/{competitionId}/standings",
                HttpMethod.GET,
                entity,
                StandingsResponse.class,
                competition.getId()
        );

        return response.getBody();
    }

    @Transactional
    public void saveStandings(List<StandingsResponse> responses) {
        Set<Team> teams = responses.stream()
                .flatMap(standingsResponse -> standingsResponse.getStandings().stream())
                .flatMap(competitionStandings -> competitionStandings.getStandings().stream())
                .map(Standing::getTeam)
                // save teams that may not be in the DB yet, since a team could have a
                // standing in a competition but not have any matches programmed soon enough to be picked up
                // by the method that fetches and saves upcoming matches
                .filter(team -> team.getId() != null)
                .collect(Collectors.toSet());
        teamService.saveTeams(teams);

        List<CompetitionStandings> standingsToSave = responses.stream()
                .flatMap(response -> response.getStandings()
                        .stream()
                        .peek(standings -> standings.setCompetition(response.getCompetition())))
                .toList();

        standingsService.saveStandings(standingsToSave);
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

        List<Match> matches = response.getBody().getMatches();
        matches = matches
                .stream()
                .filter(match -> match.getAwayTeam().getId() != null && match.getHomeTeam().getId() != null)
                .toList();

        if (!matches.isEmpty())
            this.saveMatches(matches, saveOrUpdate);
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
            matches.forEach(match -> match.setNew(false)); // entities are guaranteed to be in the DB
            matchService.updateMatches(matches);
        }
    }


    private void saveMatches(List<Match> matches, Boolean saveOrUpdate) {
        Set<Team> teams = matches.stream()
                .flatMap(match -> Stream.of(match.getHomeTeam(), match.getAwayTeam()))
                .collect(Collectors.toSet());

        teamService.saveTeams(teams);

        matches.forEach(match -> {
            // because of the APIs design all teams and competitions
            // are guaranteed to already exist in DB by this point, so this code gets
            // proxy objects for each match to avoid unnecessary selects that check for
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
}