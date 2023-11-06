package com.example.betkickapi.service;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.response.CompetitionsResponse;
import com.example.betkickapi.response.MatchesResponse;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@AllArgsConstructor
@Service
public class FootballDataService {
    private RestTemplate restTemplate;
    private Environment env;
    private final String API_URL = "https://api.football-data.org/v4/competitions";
    public List<Competition> getCompetitionsFromApi() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", env.getProperty("API_KEY"));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<CompetitionsResponse> response = restTemplate.exchange(
                API_URL,
                HttpMethod.GET,
                entity,
                CompetitionsResponse.class
        );

        return response.getBody().getCompetitions();
    }

    public List<Match> getTodayMatchesFromApi() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", env.getProperty("API_KEY"));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<MatchesResponse> response = restTemplate.exchange(
                "https://api.football-data.org/v4/matches",
                HttpMethod.GET,
                entity,
                MatchesResponse.class
        );

        return response.getBody().getMatches();
    }
}
