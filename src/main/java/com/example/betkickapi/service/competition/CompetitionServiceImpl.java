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
        // add short names
        for (Competition comp : competitions) {
            switch (comp.getCode()) {
                case ("WC"):
                    comp.setShortName("World Cup");
                    break;
                case ("CL"):
                    comp.setShortName("Champions");
                    break;
                case ("BSA"):
                    comp.setShortName("Brasileirão");
                    break;
                case ("PL"):
                    comp.setShortName("Premier");
                    break;
                case ("EC"):
                    comp.setShortName("Euro");
                    break;
                case ("PPL"):
                    comp.setShortName("Primeira");
                    break;
                case ("CLI"):
                    comp.setShortName("Libertadores");
                    break;
                case ("PD"):
                    comp.setShortName("LaLiga");
                    break;
                default:
                    // do nothing
            }
        }
        competitionRepository.saveAll(competitions);
    }

    @Override
    public Competition getReference(Integer id) {
        return competitionRepository.getReferenceById(id);
    }

    @Override
    @Cacheable(value = "footballDataCache", key = "'activeCompetitions'")
    public List<Competition> getActiveCompetitions() {
        return competitionRepository.findAllCompetitionsWithScheduledMatches();
    }

    @Override
    @Cacheable(value = "footballDataCache", key = "'competitionsWithStandings'")
    public List<Competition> getCompetitionsWithStandings() {
        return competitionRepository.findCompetitionsThatHaveStandings();
    }

    @Override
    @Cacheable(value = "footballDataCache", key = "'allCompetitions'")
    public List<Competition> getAllCompetitions() {
        return competitionRepository.findAll();
    }
}
