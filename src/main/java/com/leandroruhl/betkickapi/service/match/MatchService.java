package com.leandroruhl.betkickapi.service.match;

import com.leandroruhl.betkickapi.model.Match;

import java.util.List;

/**
 * The MatchService interface provides methods for managing operations related to matches.
 * Implementations of this interface handle interactions with match data and support functionalities
 * such as retrieving non-finished matches by IDs, checking for matches today,
 * finding matches with random odds, saving matches, etc.
 */
public interface MatchService {

    /**
     * Retrieves non-finished matches based on their IDs.
     *
     * @param ids The list of unique identifiers of matches.
     * @return A list of {@link Match} objects representing the non-finished matches.
     */
    List<Match> getNonFinishedMatchesByIds(List<Integer> ids);

    /**
     * Checks if there are matches scheduled for today.
     *
     * @return {@code true} if there are matches today; {@code false} otherwise.
     */
    Boolean areThereMatchesToday();

    /**
     * Finds matches with random odds.
     *
     * @return A list of {@link Match} objects representing matches with random odds.
     */
    List<Match> findMatchesWithRandomOdds();

    /**
     * Saves a list of matches.
     *
     * @param matches The list of {@link Match} objects to be saved.
     */
    void saveMatches(List<Match> matches);

    /**
     * Updates an individual match.
     *
     * @param updatedMatch The updated {@link Match} object.
     */
    void updateMatch(Match updatedMatch);

    /**
     * Saves or updates multiple matches.
     *
     * @param matches The list of {@link Match} objects to be saved or updated.
     */
    void saveOrUpdateMatches(List<Match> matches);

    /**
     * Updates multiple matches.
     *
     * @param matches The list of {@link Match} objects to be updated.
     */
    void updateMatches(List<Match> matches);

    /**
     * Retrieves all non-finished matches (live, scheduled, etc.).
     *
     * @return A list of all {@link Match} objects.
     */
    List<Match> getNonFinishedMatches();

    /**
     * Saves a single match.
     *
     * @param match The {@link Match} object to be saved.
     * @return The saved {@link Match} object.
     */
    Match saveMatch(Match match);

    /**
     * Retrieves matches for a specific competition based on its ID.
     *
     * @param id The unique identifier of the competition.
     * @return A list of {@link Match} objects representing matches for the specified competition.
     */
    List<Match> getMatchesByCompetitionId(Integer id);
}

