package com.example.betkickapi.dto.external_api;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.CompetitionStandings;
import lombok.Data;

import java.util.List;

/**
 * DTO representing a response containing standings for a specific competition, received from
 * <a href="https://www.football-data.org/">football-data.org API</a>.
 */
@Data
public class StandingsResponse {
    private Competition competition;
    private List<CompetitionStandings> standings;
}
