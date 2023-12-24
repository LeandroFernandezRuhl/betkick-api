package com.example.betkickapi.service.match;

import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.enums.Status;
import com.example.betkickapi.model.embbeded.MatchOdds;
import com.example.betkickapi.repository.MatchRepository;
import com.example.betkickapi.service.utility.CacheService;
import com.example.betkickapi.service.bet.BetService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Slf4j
public class MatchServiceImpl implements MatchService {
    private MatchRepository matchRepository;
    private CacheService cacheService;
    private BetService betService;

    @Override
    @Cacheable(value = "footballDataCache", key = "'matches'")
    public List<Match> getMatches() {
        Instant currentInstant = Instant.now();
        LocalDateTime currentDate = LocalDateTime.ofInstant(currentInstant, ZoneOffset.UTC)
                .withHour(0)
                .withSecond(0)
                .withNano(0);
        // get today's matches that are not finished yet (don't have a winner)
        log.info("New cache key created: matches");
        return matchRepository.findAllByUtcDateIsAfterAndWinnerIsNull(currentDate);
    }

    @Override
    @Cacheable(value = "footballDataCache", key = "'matchesByCompetitionId-' + #id")
    // beware of what's cached when this method is called but the database is empty (hint: it caches an empty array)
    // also remember to invalid cache according to the cron update
    public List<Match> getMatchesByCompetitionId(Integer id) {
        String cacheKey = "matchesByCompetitionId-" + id;
        log.info("New cache key created: " + cacheKey);
        return matchRepository.findByCompetitionId(id);
    }

    @Override
    public List<Match> getNonFinishedMatchesByIds(List<Integer> ids) {
        return matchRepository.findByIdsAndStatusIsNotFinished(ids);
    }

    @Override
    public Boolean areThereMatchesToday() {
        Instant currentInstant = Instant.now();
        LocalDate today = LocalDate.ofInstant(currentInstant, ZoneOffset.UTC);
        LocalDate tomorrow = today.plusDays(1);
        return matchRepository.existsByUtcDate(today, tomorrow);
    }

    @Override
    public List<Match> findMatchesWithRandomOdds() {
        return matchRepository.findByOdds_TemporaryRandomOddsIsTrue(PageRequest.of(0, 3));
    }

    @Override
    public void saveMatches(List<Match> matches) {
        // matches = matches.stream().filter(match -> !matchRepository.existsById(match.getId()))
        //         .collect(Collectors.toList());
        // this makes a matches.size() amount of JDBC statements, the one below makes just one and is ~5x faster

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

    @Override
    public void updateMatches(List<Match> newMatches) {
        List<Integer> matchIds = newMatches.stream()
                .map(Match::getId)
                .toList();

        Map<Integer, Match> matchesInDb = matchRepository
                .findMatchesByIds(matchIds)
                .stream()
                .collect(Collectors.toMap(Match::getId, match -> match));
        // avoids N select queries by directly fetching all the matches
        // that are necessary for the updates, this is ~19x faster than
        // doing just a normal saveAll and letting Hibernate fetch each
        // required entity individually

        newMatches.forEach(newMatch -> {
            Match matchInDb = matchesInDb.get(newMatch.getId());
            matchInDb.setScore(newMatch.getScore());
            matchInDb.setDuration(newMatch.getDuration());
            matchInDb.setUtcDate(newMatch.getUtcDate());
            // a match has finished so bet earnings have to be paid
            if ((matchInDb.getStatus() != Status.FINISHED && matchInDb.getStatus() != Status.AWARDED)
                    && (newMatch.getStatus() == Status.FINISHED || newMatch.getStatus() == Status.AWARDED)) {
                betService.finishBets(newMatch);
            }
            matchInDb.setStatus(newMatch.getStatus());
        });

        List<Match> savedMatches = matchRepository.saveAll(newMatches);
        invalidateMatchesCache(savedMatches);
    }

    @Override
    public void updateMatch(Match updatedMatch) {
        Match matchInDb = matchRepository.findById(updatedMatch.getId()).get();
        matchInDb.setOdds(updatedMatch.getOdds());
        matchRepository.save(matchInDb);
        // cache is invalidated once per minute when doing batch updates, no need to do it here too
    }

    private void invalidateMatchesCache(List<Match> savedMatches) {
        List<String> cacheKeys = savedMatches
                .stream()
                .map(match -> {
                    return "matchesByCompetitionId-" + match.getCompetition().getId();
                })
                .distinct()
                .toList();
        cacheService.invalidateCacheForKeys(cacheKeys);
        cacheService.invalidateCacheForKey("matches");
    }

    @Override
    public void saveOrUpdateMatches(List<Match> matches) {
        // get IDs of matches that already exist
        List<Integer> existingMatchIds = matchRepository.findExistingMatchIds(
                matches.stream().map(Match::getId).collect(Collectors.toList())
        );

        // save new matches
        List<Match> newMatches = matches.stream()
                .filter(match -> !existingMatchIds.contains(match.getId()))
                .toList();
        newMatches.forEach(match -> match.setOdds(new MatchOdds().generateRandomOdds()));
        List<Match> savedMatches = matchRepository.saveAll(newMatches);
        invalidateMatchesCache(savedMatches);

        // update existent matches
        List<Match> matchesInDb = matches.stream()
                .filter(match -> existingMatchIds.contains(match.getId()))
                .toList();
        matchesInDb.forEach(match -> match.setNew(false)); // entities are guaranteed to exist in the DB, so they're not new

        this.updateMatches(matchesInDb);
    }
}
