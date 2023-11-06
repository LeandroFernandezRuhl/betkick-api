package com.example.betkickapi.response;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.model.Match;
import lombok.Data;

import java.util.List;

@Data
public class MatchesResponse {
    private List<Match> matches;
}
