package com.example.betkickapi.service.match;

import com.example.betkickapi.model.Match;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface MatchService {
    Boolean areThereMatchesToday();

    void saveMatches(List<Match> matches);

    void saveOrUpdateMatches(List<Match> matches);

    void updateMatches(List<Match> matches);

    List<Match> getMatches();

    List<Match> getMatchesByCompetitionId(Integer id);
}
