package com.example.betkickapi.service.team;

import com.example.betkickapi.model.Team;

import java.util.Set;

public interface TeamService {
    void saveTeams(Set<Team> teams);

    Team getReference(Integer id);
}
