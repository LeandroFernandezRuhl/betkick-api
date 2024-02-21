package com.example.betkickapi.service.utility;

import com.example.betkickapi.dto.external_api.*;
import com.example.betkickapi.model.*;
import com.example.betkickapi.service.competition.CompetitionService;
import com.example.betkickapi.service.match.MatchService;
import com.example.betkickapi.service.standings.StandingsService;
import com.example.betkickapi.service.team.TeamService;
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

/**
 * TheFootballApiService class provides methods for interacting with the <a href="https://www.football-data.org/">football-data.org API</a>,
 * fetching various statistics, match information, and competition details. It encapsulates the
 * functionality related to retrieving and managing data from the API.
 * <br>
 * <br>
 * This class utilizes the Spring Framework's RestTemplate for making HTTP requests and employs various services
 * such as {@link CompetitionService}, {@link MatchService}, {@link TeamService}, and {@link StandingsService} for
 * handling specific functionalities.
 */
@Service
@Slf4j
public class FootballApiService {

    private final RestTemplate restTemplate;
    private final String API_KEY;
    private final CompetitionService competitionService;
    private final MatchService matchService;
    private final TeamService teamService;
    private final StandingsService standingsService;

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

    /**
     * Fetches the number of wins, draws, and losses of a team within a specified date range.
     *
     * @param teamId   The unique identifier of the team for which statistics are to be fetched.
     * @param dateFrom The start date of the period for which statistics are retrieved.
     * @param dateTo   The end date of the period for which statistics are retrieved.
     * @return A {@link TeamStatsResponse} object containing the requested team statistics.
     */
    public TeamStatsResponse fetchTeamStats(Integer teamId, LocalDate dateFrom, LocalDate dateTo) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Stats extracted from "resultSet" object in the JSON response
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

    /**
     * Fetches head-to-head statistics for a specific match from the API.
     *
     * @param matchId The unique identifier of the match for which head-to-head statistics are to be fetched.
     * @return A {@link HeadToHeadResponse} object containing the requested head-to-head statistics.
     */
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

    /**
     * Fetches a list of football competitions from the API.
     *
     * @return A list of {@link Competition} objects representing football competitions.
     */
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

    /**
     * Fetches the standings of a specific competition from the API.
     *
     * @param competition The {@link Competition} for which standings are to be fetched.
     * @return A {@link StandingsResponse} object containing the requested standings.
     */
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

    /**
     * Saves the provided standings responses to the database. This includes updating or saving teams and competition
     * standings based on the retrieved data.
     *
     * @param responses A list of {@link StandingsResponse} objects representing the standings data.
     * @see TeamService#saveTeams(Set)
     * @see StandingsService#saveStandings(List)
     */
    @Transactional
    public void saveStandings(List<StandingsResponse> responses) {
        // Save teams that may not be in the DB yet, since a team could have a
        // standing in a competition but not have any matches programmed soon enough to be picked up
        // by the method that fetches and saves upcoming matches
        Set<Team> teams = responses.stream()
                .flatMap(standingsResponse -> standingsResponse.getStandings().stream())
                .flatMap(competitionStandings -> competitionStandings.getStandings().stream())
                .map(Standing::getTeam)
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

    /**
     * Fetches and saves matches from the API within the specified date range, optionally updating existing matches
     * in the database.
     *
     * @param dateFrom     The start date for fetching matches.
     * @param dateTo       The end date for fetching matches.
     * @param saveOrUpdate A boolean indicating whether to save or update existing matches in the database.
     * @see MatchesResponse
     * @see MatchService#saveMatches(List)
     * @see MatchService#saveOrUpdateMatches(List)
     */
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

    /**
     * Fetches today's matches from the API and updates the existing matches in the database.
     *
     * @see MatchesResponse
     * @see MatchService#updateMatches(List)
     */
    @Transactional
    public void fetchAndUpdateMatches() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // This only gets today's games
        ResponseEntity<MatchesResponse> response = restTemplate.exchange(
                "https://api.football-data.org/v4/matches",
                HttpMethod.GET,
                entity,
                MatchesResponse.class
        );

        List<Match> matches = response.getBody().getMatches();
        if (matches.size() > 0) {
            matches.forEach(match -> match.setNew(false)); // Entities are guaranteed to be in the DB
            matchService.updateMatches(matches);
        }
    }

    /**
     * Saves the provided list of matches to the database, including updating or saving associated teams and competitions.
     *
     * @param matches      A list of {@link Match} objects representing the matches to be saved.
     * @param saveOrUpdate A boolean indicating whether to save or update existing matches in the database.
     * @see TeamService#saveTeams(Set)
     * @see MatchService#saveMatches(List)
     * @see MatchService#saveOrUpdateMatches(List)
     */
    private void saveMatches(List<Match> matches, Boolean saveOrUpdate) {
        Set<Team> teams = matches.stream()
                .flatMap(match -> Stream.of(match.getHomeTeam(), match.getAwayTeam()))
                .collect(Collectors.toSet());

        teamService.saveTeams(teams);

        // Because of the APIs design all teams and competitions
        // are guaranteed to already exist in DB by this point, so this code gets
        // proxy objects for each match to avoid unnecessary selects that check for
        // the entities existence
        matches.forEach(match -> {
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