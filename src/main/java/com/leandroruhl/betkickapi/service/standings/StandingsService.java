package com.leandroruhl.betkickapi.service.standings;

import com.leandroruhl.betkickapi.dto.internal_api.CompetitionStandingsResponse;
import com.leandroruhl.betkickapi.model.CompetitionStandings;
import com.leandroruhl.betkickapi.model.Standing;

import java.util.List;

/**
 * The StandingsService interface provides methods for managing operations related to standings in competitions.
 * Implementations of this interface handle interactions with standings data and support functionalities
 * such as retrieving standings by competition ID, saving standings, deleting standings, etc.
 */
public interface StandingsService {

    /**
     * Retrieves the standings for a specific competition based on its ID.
     *
     * @param competitionId The unique identifier of the competition.
     * @return A list of {@link CompetitionStandingsResponse} objects representing the standings for the competition.
     */
    List<CompetitionStandingsResponse> getStandingsByCompetitionId(Integer competitionId);

    /**
     * Saves a list of standings for competitions.
     *
     * @param standings The list of {@link CompetitionStandings} objects to be saved.
     */
    void saveStandings(List<CompetitionStandings> standings);

    /**
     * Deletes all standings.
     */
    void deleteStandings();

    /**
     * Retrieves the standings for a specific competition and teams based on their IDs.
     *
     * @param compId     The unique identifier of the competition.
     * @param homeTeamId The unique identifier of the home team.
     * @param awayTeamId The unique identifier of the away team.
     * @return A list of {@link Standing} objects representing the standings for the specified competition and teams.
     */
    List<Standing> getStandingsByCompIdAndTeams(Integer compId, Integer homeTeamId, Integer awayTeamId);

    /**
     * Counts the number of standings for a specific competition based on its ID.
     *
     * @param compId The unique identifier of the competition.
     * @return The number of standings for the specified competition.
     */
    Integer countStandingsByCompId(Integer compId);
}

