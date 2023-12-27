package com.example.betkickapi.web.internal;

import com.example.betkickapi.model.Competition;
import lombok.Data;

import java.util.List;

@Data
public class CompetitionStandingsResponse {
    private Competition competition;
    private String group;
    private List<StandingResponse> standings;
}
