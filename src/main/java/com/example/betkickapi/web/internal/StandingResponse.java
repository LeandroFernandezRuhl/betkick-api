package com.example.betkickapi.web.internal;

import com.example.betkickapi.model.Team;
import lombok.Data;

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