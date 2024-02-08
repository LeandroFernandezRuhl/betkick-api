package com.example.betkickapi.service.competition;

import com.example.betkickapi.model.Competition;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface CompetitionService {
    void saveCompetition(Competition competition);

    void saveCompetitions(List<Competition> competitions);

    Competition getReference(Integer id);

    List<Competition> getActiveCompetitions();

    List<Competition> getCompetitionsWithStandings();

    List<Competition> getAllCompetitions();
}
