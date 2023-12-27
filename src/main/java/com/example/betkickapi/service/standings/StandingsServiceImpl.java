package com.example.betkickapi.service.standings;

import com.example.betkickapi.model.CompetitionStandings;
import com.example.betkickapi.model.Standing;
import com.example.betkickapi.repository.CompetitionStandingsRepository;
import com.example.betkickapi.repository.StandingRepository;
import com.example.betkickapi.service.team.TeamService;
import com.example.betkickapi.service.utility.CacheService;
import com.example.betkickapi.web.internal.CompetitionStandingsResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
@Slf4j
public class StandingsServiceImpl implements StandingsService {
    private CompetitionStandingsRepository competitionStandingsRepository;
    private StandingRepository standingRepository;
    private TeamService teamService;
    private ModelMapper modelMapper;
    private CacheService cacheService;

    @Override
    @Cacheable(value = "footballDataCache", key = "'standingsByCompetitionId-' + #id")
    // beware of what's cached when this method is called but the database is empty (it caches an empty array)
    public List<CompetitionStandingsResponse> getStandingsByCompetitionId(Integer id) {
        String cacheKey = "standingsByCompetitionId-" + id;
        log.info("New cache key created: " + cacheKey);
        return convertStandingsToDto(competitionStandingsRepository.findByCompetitionId(id));
    }

    private List<CompetitionStandingsResponse> convertStandingsToDto(List<CompetitionStandings> standings) {
        return standings
                .stream()
                .map(standing -> modelMapper.map(standing, CompetitionStandingsResponse.class))
                .toList();
    }

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
                        // get proxy objects to avoid unnecessary selects before insert
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

    private void invalidateStandingsCache(List<CompetitionStandings> savedStandings) {
        List<String> cacheKeys = savedStandings
                .stream()
                .map(standing -> "standingsByCompetitionId-" + standing.getCompetition().getId())
                .distinct()
                .toList();
        cacheService.invalidateCacheForKeys(cacheKeys);
    }

    @Override
    @Transactional
    public void deleteStandings() {
        standingRepository.deleteAll();
        competitionStandingsRepository.deleteAll();
    }
}
