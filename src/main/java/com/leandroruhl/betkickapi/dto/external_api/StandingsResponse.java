package com.leandroruhl.betkickapi.dto.external_api;

import com.leandroruhl.betkickapi.model.Competition;
import com.leandroruhl.betkickapi.model.CompetitionStandings;
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
