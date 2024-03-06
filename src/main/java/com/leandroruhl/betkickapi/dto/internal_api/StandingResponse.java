package com.leandroruhl.betkickapi.dto.internal_api;

import com.leandroruhl.betkickapi.model.Team;
import lombok.Data;

/**
 * DTO representing the response for individual team standings in a competition.
 */
@Data
public class StandingResponse {
    private Integer position;
    private Team team;
    private Integer won;
    private Integer draw;
    private Integer lost;
    private Integer points;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer goalDifference;
}
