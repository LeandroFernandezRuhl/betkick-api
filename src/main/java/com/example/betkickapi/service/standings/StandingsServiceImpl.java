package com.example.betkickapi.service.standings;

import com.example.betkickapi.dto.internal_api.CompetitionStandingsResponse;
import com.example.betkickapi.model.CompetitionStandings;
import com.example.betkickapi.model.Standing;
import com.example.betkickapi.repository.CompetitionStandingsRepository;
import com.example.betkickapi.repository.StandingRepository;
import com.example.betkickapi.service.team.TeamService;
import com.example.betkickapi.service.utility.CacheService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The StandingsServiceImpl class implements the {@link StandingsService} interface
 * and provides concrete implementations for managing operations related to standings in competitions.
 * This class utilizes a {@link CompetitionStandingsRepository}, {@link StandingRepository},
 * {@link TeamService}, {@link ModelMapper}, and {@link CacheService} for interacting with standings data.
 */
@AllArgsConstructor
@Service
@Slf4j
public class StandingsServiceImpl implements StandingsService {

    private final CompetitionStandingsRepository competitionStandingsRepository;
    private final StandingRepository standingRepository;
    private final TeamService teamService;
    private final ModelMapper modelMapper;
    private final CacheService cacheService;

    /**
     * This implementation retrieves the standings for a specific competition based on its ID.
     * It utilizes caching to improve performance, and a cache key is generated based on the competition ID.
     *
     * @param id The unique identifier of the competition.
     * @return A list of {@link CompetitionStandingsResponse} objects representing the standings for the competition.
     */
    @Override
    @Cacheable(value = "footballDataCache", key = "'standingsByCompetitionId-' + #id")
    // WARNING: if this method is called while the database is empty it caches an empty array
    public List<CompetitionStandingsResponse> getStandingsByCompetitionId(Integer id) {
        String cacheKey = "standingsByCompetitionId-" + id;
        log.info("New cache key created: " + cacheKey);
        return convertStandingsToDto(competitionStandingsRepository.findByCompetitionId(id));
    }

    /**
     * Converts a list of {@link CompetitionStandings} entities to a list of {@link CompetitionStandingsResponse} DTOs.
     *
     * @param standings The list of {@link CompetitionStandings} entities to be converted.
     * @return A list of {@link CompetitionStandingsResponse} DTOs representing the converted standings.
     */
    private List<CompetitionStandingsResponse> convertStandingsToDto(List<CompetitionStandings> standings) {
        return standings
                .stream()
                .map(standing -> modelMapper.map(standing, CompetitionStandingsResponse.class))
                .toList();
    }

    /**
     * This implementation saves a list of standings for competitions.
     * It uses a transaction to ensure atomicity and consistency of the database operations.
     *
     * @param standings The list of {@link CompetitionStandings} objects to be saved.
     */
    @Override
    @Transactional
    public void saveStandings(List<CompetitionStandings> standings) {
        List<CompetitionStandings> savedStandings = competitionStandingsRepository.saveAll(standings);

        standings.forEach(compStanding -> {
            List<Standing> teamStandings = compStanding.getStandings();
            compStanding.setStandings(teamStandings
                    .stream()
                    .filter(teamStanding -> teamStanding.getTeam().getId() != null)
                    .peek(teamStanding -> {
                        // Get proxy objects to avoid unnecessary selects before insert
                        Integer teamId = teamStanding.getTeam().getId();
                        teamStanding.setTeam(teamService.getReference(teamId));
                        teamStanding.setCompetition(compStanding);
                    })
                    .toList());
        });

        standingRepository.saveAll(standings
                .stream()
                .flatMap(compStandings -> compStandings.getStandings().stream())
                .toList());

        invalidateStandingsCache(savedStandings);
    }

    /**
     * Invalidates the cache for the standings associated with the competitions in the given list.
     *
     * @param savedStandings The list of {@link CompetitionStandings} entities for which the cache needs to be invalidated.
     */
    private void invalidateStandingsCache(List<CompetitionStandings> savedStandings) {
        List<String> cacheKeys = savedStandings
                .stream()
                .map(standing -> "standingsByCompetitionId-" + standing.getCompetition().getId())
                .distinct()
                .toList();

        cacheService.invalidateCacheForKeys(cacheKeys);
    }

    /**
     * This implementation deletes all standings.
     * It uses a transaction to ensure atomicity and consistency of the database operations.
     */
    @Override
    @Transactional
    public void deleteStandings() {
        standingRepository.deleteAll();
        competitionStandingsRepository.deleteAll();
    }

    /**
     * This implementation retrieves the standings for a specific competition and teams based on their IDs.
     *
     * @param compId     The unique identifier of the competition.
     * @param homeTeamId The unique identifier of the home team.
     * @param awayTeamId The unique identifier of the away team.
     * @return A list of {@link Standing} objects representing the standings for the specified competition and teams.
     */
    @Override
    public List<Standing> getStandingsByCompIdAndTeams(Integer compId, Integer homeTeamId, Integer awayTeamId) {
        return standingRepository.findStandingsByCompAndTeams(compId, homeTeamId, awayTeamId);
    }

    /**
     * This implementation counts the number of standings for a specific competition based on its ID.
     *
     * @param compId The unique identifier of the competition.
     * @return The number of standings for the specified competition.
     */
    @Override
    public Integer countStandingsByCompId(Integer compId) {
        return standingRepository.countStandingsByCompId(compId);
    }
}
