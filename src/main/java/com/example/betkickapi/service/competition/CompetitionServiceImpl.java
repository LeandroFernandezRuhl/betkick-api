package com.example.betkickapi.service.competition;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.repository.CompetitionRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The CompetitionServiceImpl class implements the {@link CompetitionService} interface
 * and provides concrete implementations for managing operations related to competitions.
 * This class utilizes a {@link CompetitionRepository} for interacting with competition data.
 */
@AllArgsConstructor
@Service
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionRepository competitionRepository;

    /**
     * This implementation saves a single competition using the {@link CompetitionRepository#save(Object)} method.
     *
     * @param competition The {@link Competition} object to be saved.
     */
    @Override
    public void saveCompetition(Competition competition) {
        competitionRepository.save(competition);
    }

    /**
     * This implementation saves a list of competitions using the {@link CompetitionRepository#saveAll(java.lang.Iterable)} method.
     * Additionally, it adds short names to the competitions based on their codes for better readability.
     *
     * @param competitions The list of {@link Competition} objects to be saved.
     */
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
                    // Do nothing
            }
        }
        competitionRepository.saveAll(competitions);
    }

    /**
     * This implementation retrieves a reference to a competition using the CompetitionRepository.getReferenceById(Integer) method.
     *
     * @param id The unique identifier of the competition.
     * @return A reference to the {@link Competition} object.
     * @see CompetitionRepository
     */
    @Override
    public Competition getReference(Integer id) {
        return competitionRepository.getReferenceById(id);
    }

    /**
     * This implementation retrieves a list of active competitions with scheduled matches, utilizing caching for improved performance.
     *
     * @return A list of {@link Competition} objects representing active competitions.
     */
    @Override
    @Cacheable(value = "footballDataCache", key = "'activeCompetitions'")
    public List<Competition> getActiveCompetitions() {
        return competitionRepository.findAllCompetitionsWithScheduledMatches();
    }

    /**
     * This implementation retrieves a list of competitions that have standings, utilizing caching for improved performance.
     *
     * @return A list of {@link Competition} objects representing competitions with standings.
     */
    @Override
    @Cacheable(value = "footballDataCache", key = "'competitionsWithStandings'")
    public List<Competition> getCompetitionsWithStandings() {
        return competitionRepository.findCompetitionsThatHaveStandings();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation retrieves a list of all competitions, utilizing caching for improved performance.
     *
     * @return A list of all {@link Competition} objects.
     */
    @Override
    @Cacheable(value = "footballDataCache", key = "'allCompetitions'")
    public List<Competition> getAllCompetitions() {
        return competitionRepository.findAll();
    }
}

