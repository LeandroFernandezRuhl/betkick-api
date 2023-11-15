package com.example.betkickapi.service.competition;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.repository.CompetitionRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CompetitionServiceImpl implements CompetitionService {
    private CompetitionRepository competitionRepository;

    @Override
    public void saveCompetition(Competition competition) {
        competitionRepository.save(competition);
    }

    @Override
    public void saveCompetitions(List<Competition> competitions) {
        competitionRepository.saveAll(competitions);
    }

    @Override
    public Competition getReference(Integer id) {
        return competitionRepository.getReferenceById(id);
    }

    @Override
    @Cacheable(value = "footballDataCache", key = "'allCompetitions'")
    // remember to make an update method with @CachePut (?)
    public List<Competition> getCompetitions() {
        return competitionRepository.findAll();
    }
}
