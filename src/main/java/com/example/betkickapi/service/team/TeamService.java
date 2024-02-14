package com.example.betkickapi.service.team;

import com.example.betkickapi.model.Team;

import java.util.Set;

/**
 * The TeamService interface defines methods for managing operations related to teams.
 * Implementations of this interface handle interactions with team data and support functionalities
 * such as saving multiple teams, saving a single team, and obtaining a lightweight reference to a team.
 */
public interface TeamService {

    /**
     * Saves a set of teams.
     *
     * @param teams The set of {@link Team} objects to be saved.
     * @see Team
     */
    void saveTeams(Set<Team> teams);

    /**
     * Saves a single team.
     *
     * @param team The {@link Team} object to be saved.
     * @return The saved {@link Team} object.
     * @see Team
     */
    Team saveTeam(Team team);

    /**
     * Gets a lightweight reference to a team based on its ID.
     *
     * @param id The unique identifier of the team.
     * @return A reference to the {@link Team} object with the specified ID.
     * @see Team
     */
    Team getReference(Integer id);
}

