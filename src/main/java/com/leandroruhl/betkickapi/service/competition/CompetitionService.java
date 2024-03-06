package com.leandroruhl.betkickapi.service.competition;

import com.leandroruhl.betkickapi.model.Competition;

import java.util.List;

/**
 * The CompetitionService interface provides methods for managing operations related to competitions.
 * Implementations of this interface handle interactions with competition data and support functionalities
 * such as saving a competition, saving a list of competitions, retrieving competitions with standings, etc.
 */
public interface CompetitionService {

    /**
     * Saves a single competition.
     *
     * @param competition The {@link Competition} object to be saved.
     */
    void saveCompetition(Competition competition);

    /**
     * Saves a list of competitions.
     *
     * @param competitions The list of {@link Competition} objects to be saved.
     */
    void saveCompetitions(List<Competition> competitions);

    /**
     * Retrieves a reference to a competition based on its ID.
     *
     * @param id The unique identifier of the competition.
     * @return A reference to the {@link Competition} object.
     */
    Competition getReference(Integer id);

    /**
     * Retrieves a list of active competitions.
     *
     * @return A list of {@link Competition} objects representing active competitions.
     */
    List<Competition> getActiveCompetitions();

    /**
     * Retrieves a list of competitions with standings.
     *
     * @return A list of {@link Competition} objects representing competitions with standings.
     */
    List<Competition> getCompetitionsWithStandings();

    /**
     * Retrieves a list of all competitions.
     *
     * @return A list of all {@link Competition} objects.
     */
    List<Competition> getAllCompetitions();
}

