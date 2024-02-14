package com.example.betkickapi.service.match;

import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.embbeded.MatchOdds;
import com.example.betkickapi.model.enums.Status;
import com.example.betkickapi.repository.MatchRepository;
import com.example.betkickapi.service.bet.BetService;
import com.example.betkickapi.service.utility.CacheService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The MatchServiceImpl class implements the {@link MatchService} interface
 * and provides concrete implementations for managing operations related to matches.
 * <br>
 * <br>
 * This class utilizes a {@link MatchRepository}, {@link CacheService}, and {@link BetService}
 * for interacting with match data, caching, and handling bets.
 */
@AllArgsConstructor
@Service
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final CacheService cacheService;
    private final BetService betService;

    /**
     * This implementation retrieves all non-finished matches in the DB, utilizing caching for improved performance.
     *
     * @return A list of {@link Match} objects representing non-finished matches for today.
     */
    @Override
    @Cacheable(value = "footballDataCache", key = "'matches'")
    public List<Match> getNonFinishedMatches() {
        // Get all matches that are not finished yet (those that don't have a winner)
        log.info("New cache key created: matches");
        return matchRepository.findAllUnfinishedMatches();
    }

    /**
     * This implementation saves a single match.
     *
     * @param match The {@link Match} object to be saved.
     * @return The saved {@link Match} object.
     */
    @Override
    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }

    /**
     * This implementation retrieves matches for a specific competition based on its ID, utilizing caching for improved performance.
     * A cache key is generated based on the competition ID.
     *
     * @param id The unique identifier of the competition.
     * @return A list of {@link Match} objects representing matches for the specified competition.
     */
    @Override
    @Cacheable(value = "footballDataCache", key = "'matchesByCompetitionId-' + #id")
    // WARNING: if this method is called while the database is empty it caches an empty array
    public List<Match> getMatchesByCompetitionId(Integer id) {
        String cacheKey = "matchesByCompetitionId-" + id;
        log.info("New cache key created: " + cacheKey);
        return matchRepository.findByCompetitionId(id);
    }

    /**
     * This implementation retrieves non-finished matches based on their IDs.
     *
     * @param ids The list of unique identifiers of matches.
     * @return A list of {@link Match} objects representing the non-finished matches.
     */
    @Override
    public List<Match> getNonFinishedMatchesByIds(List<Integer> ids) {
        return matchRepository.findByIdsAndStatusIsNotFinished(ids);
    }

    /**
     * This implementation checks if there are matches scheduled for today.
     *
     * @return {@code true} if there are matches today; {@code false} otherwise.
     */
    @Override
    public Boolean areThereMatchesToday() {
        Instant currentInstant = Instant.now();
        LocalDate today = LocalDate.ofInstant(currentInstant, ZoneOffset.UTC);
        LocalDate tomorrow = today.plusDays(1);
        return matchRepository.existsByUtcDate(today, tomorrow);
    }

    /**
     * This implementation finds matches with random odds.
     *
     * @return A list of {@link Match} objects representing matches with random odds.
     */
    @Override
    public List<Match> findMatchesWithRandomOdds() {
        return matchRepository.findByOdds_TemporaryRandomOddsIsTrue(PageRequest.of(0, 3));
    }

    /**
     * This implementation saves a list of matches.
     * It generates random odds for new matches and invalidates the cache after saving.
     *
     * @param matches The list of {@link Match} objects to be saved.
     */
    @Override
    public void saveMatches(List<Match> matches) {
        List<Integer> existingMatchIds = matchRepository.findExistingMatchIds(
                matches.stream().map(Match::getId).collect(Collectors.toList())
        );

        List<Match> newMatches = matches.stream()
                .filter(match -> !existingMatchIds.contains(match.getId()))
                .toList();
        newMatches.forEach(match -> match.setOdds(new MatchOdds().generateRandomOdds()));
        List<Match> savedMatches = matchRepository.saveAll(newMatches);
        invalidateMatchesCache(savedMatches);
    }

    /**
     * This implementation updates multiple matches.
     * It avoids unnecessary selects before insert by fetching all the necessary matches in one query.
     * Bets associated with finished matches are settled.
     * The cache is invalidated after updating the matches.
     *
     * @param unsavedUpdatedMatches The list of updated {@link Match} objects.
     */
    @Override
    public void updateMatches(List<Match> unsavedUpdatedMatches) {
        List<Integer> matchIds = unsavedUpdatedMatches.stream()
                .map(Match::getId)
                .toList();

        Map<Integer, Match> nonUpdatedMatchesInDb = matchRepository
                .findMatchesByIds(matchIds)
                .stream()
                .collect(Collectors.toMap(Match::getId, match -> match));

        List<Match> updatedMatchesToSave = unsavedUpdatedMatches
                .stream()
                .map(updatedMatch -> {
                    Match matchInDb = nonUpdatedMatchesInDb.get(updatedMatch.getId());
                    matchInDb.setScore(updatedMatch.getScore());
                    matchInDb.setDuration(updatedMatch.getDuration());
                    matchInDb.setUtcDate(updatedMatch.getUtcDate());
                    matchInDb.setWinner(updatedMatch.getWinner());
                    // A match has finished so bet earnings have to be paid
                    if ((matchInDb.getStatus() != Status.FINISHED && matchInDb.getStatus() != Status.AWARDED)
                            && (updatedMatch.getStatus() == Status.FINISHED || updatedMatch.getStatus() == Status.AWARDED)) {
                        betService.finishBets(updatedMatch);
                    }
                    matchInDb.setStatus(updatedMatch.getStatus());
                    return matchInDb;
                })
                .toList();

        List<Match> savedMatches = matchRepository.saveAll(updatedMatchesToSave);
        invalidateMatchesCache(savedMatches);
    }

    /**
     * This implementation updates a single match.
     * The cache is not invalidated here as it is done once per minute during batch updates.
     *
     * @param updatedMatch The updated {@link Match} object.
     */
    @Override
    public void updateMatch(Match updatedMatch) {
        Match matchInDb = matchRepository.findById(updatedMatch.getId()).get();
        matchInDb.setOdds(updatedMatch.getOdds());
        matchRepository.save(matchInDb);
    }

    /**
     * Invalidates the cache for matches after saving or updating.
     *
     * @param savedMatches The list of saved or updated {@link Match} objects.
     */
    private void invalidateMatchesCache(List<Match> savedMatches) {
        List<String> cacheKeys = savedMatches
                .stream()
                .map(match -> "matchesByCompetitionId-" + match.getCompetition().getId())
                .distinct()
                .toList();
        cacheService.invalidateCacheForKeys(cacheKeys);
        cacheService.invalidateCacheForKey("matches");
    }

    /**
     * This implementation saves or updates a list of matches.
     * It generates random odds for new matches and invalidates the cache after saving.
     * For existent matches, it marks them as not new and updates them.
     *
     * @param matches The list of {@link Match} objects to be saved or updated.
     */
    @Override
    public void saveOrUpdateMatches(List<Match> matches) {
        // Get IDs of matches that already exist
        List<Integer> existingMatchIds = matchRepository.findExistingMatchIds(
                matches.stream().map(Match::getId).collect(Collectors.toList())
        );

        // Filter out existent matches and save the new ones
        List<Match> newMatches = matches.stream()
                .filter(match -> !existingMatchIds.contains(match.getId()))
                .toList();
        newMatches.forEach(match -> match.setOdds(new MatchOdds().generateRandomOdds()));
        List<Match> savedMatches = matchRepository.saveAll(newMatches);
        invalidateMatchesCache(savedMatches);

        // Update existent matches
        List<Match> matchesToUpdate = matches.stream()
                .filter(match -> existingMatchIds.contains(match.getId()))
                .toList();
        matchesToUpdate.forEach(match -> match.setNew(false)); // Entities are guaranteed to exist in the DB, so they're not new

        this.updateMatches(matchesToUpdate);
    }
}
