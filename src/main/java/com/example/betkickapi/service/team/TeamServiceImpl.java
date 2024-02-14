package com.example.betkickapi.service.team;

import com.example.betkickapi.model.Team;
import com.example.betkickapi.repository.TeamRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * The TeamServiceImpl class implements the {@link TeamService} interface
 * and provides concrete implementations for managing operations related to teams.
 * This class utilizes a {@link TeamRepository} for interacting with team data.
 *
 * @see TeamService
 * @see TeamRepository
 */
@AllArgsConstructor
@Service
@Slf4j
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation saves a set of teams by invoking the
     * {@link TeamRepository#findAllTeams()} and {@link TeamRepository#saveAll(Iterable)} methods.
     *
     * @param teams The set of {@link Team} objects to be saved.
     */
    @Override
    public void saveTeams(Set<Team> teams) {
        Set<Team> savedTeams = teamRepository.findAllTeams();
        teams.removeAll(savedTeams);
        teamRepository.saveAll(teams);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation saves a single team by invoking the {@link TeamRepository#save(Object)} method.
     *
     * @param team The {@link Team} object to be saved.
     * @return The saved {@link Team} object.
     */
    @Override
    public Team saveTeam(Team team) {
        return teamRepository.save(team);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation gets a lightweight reference to a team based on its ID
     * by invoking the TeamRepository.getReferenceById(Integer) method.
     *
     * @param id The unique identifier of the team.
     * @return A reference to the {@link Team} object with the specified ID.
     */
    @Override
    public Team getReference(Integer id) {
        return teamRepository.getReferenceById(id);
    }
}

