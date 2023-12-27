package com.example.betkickapi.web.externalApi;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.CompetitionStandings;
import lombok.Data;

import java.util.List;

@Data
public class StandingsResponse {
    private Competition competition;
    private List<CompetitionStandings> standings;
}
