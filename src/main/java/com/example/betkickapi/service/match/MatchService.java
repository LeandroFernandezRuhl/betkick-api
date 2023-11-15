package com.example.betkickapi.service.match;

import com.example.betkickapi.model.Match;

import java.util.List;

public interface MatchService {
    void saveMatch(Match match);

    void saveMatches(List<Match> matches);

    // make get todays matches method this gets all matches in db
    List<Match> getMatches();
}
