package com.leandroruhl.betkickapi.dto.internal_api;

import com.leandroruhl.betkickapi.model.Competition;
import lombok.Data;

import java.util.List;

/**
 * DTO representing the response for competition standings.
 */
@Data
public class CompetitionStandingsResponse {
    private Competition competition;
    private String group;
    private List<StandingResponse> standings;
}
