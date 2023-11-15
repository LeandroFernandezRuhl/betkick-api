package com.example.betkickapi.service.competition;

import com.example.betkickapi.model.Competition;

import java.util.List;

public interface CompetitionService {
    void saveCompetition(Competition competition);

    void saveCompetitions(List<Competition> competitions);

    Competition getReference(Integer id);

    List<Competition> getCompetitions();
}
