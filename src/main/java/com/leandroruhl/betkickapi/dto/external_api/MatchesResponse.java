package com.leandroruhl.betkickapi.dto.external_api;

import com.leandroruhl.betkickapi.model.Match;
import lombok.Data;

import java.util.List;

/**
 * DTO representing a response containing a list of matches, received from
 * <a href="https://www.football-data.org/">football-data.org API</a>.
 */
@Data
public class MatchesResponse {
    private List<Match> matches;
}
