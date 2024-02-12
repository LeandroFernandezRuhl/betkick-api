package com.example.betkickapi.service.team;

import com.example.betkickapi.model.Team;
import com.example.betkickapi.repository.TeamRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@AllArgsConstructor
@Service
@Slf4j
public class TeamServiceImpl implements TeamService {
    private TeamRepository teamRepository;

    @Override
    public void saveTeams(Set<Team> teams) {
        Set<Team> savedTeams = teamRepository.findAllTeams();
        teams.removeAll(savedTeams);
        teamRepository.saveAll(teams);
    }

    @Override
    public Team saveTeam(Team team) {
        return teamRepository.save(team);
    }

    @Override
    public Team getReference(Integer id) {
        return teamRepository.getReferenceById(id);
    }
}
