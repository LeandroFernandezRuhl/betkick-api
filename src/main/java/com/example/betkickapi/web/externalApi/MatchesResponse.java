package com.example.betkickapi.web.externalApi;

import com.example.betkickapi.model.Match;
import lombok.Data;

import java.util.List;

@Data
public class MatchesResponse {
    private List<Match> matches;
}
