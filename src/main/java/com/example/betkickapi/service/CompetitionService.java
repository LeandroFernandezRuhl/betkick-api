package com.example.betkickapi.service;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.response.CompetitionsResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class CompetitionService {
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
}
