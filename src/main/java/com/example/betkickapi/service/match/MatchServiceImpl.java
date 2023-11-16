package com.example.betkickapi.service.match;

import com.example.betkickapi.model.Match;
import com.example.betkickapi.repository.MatchRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class MatchServiceImpl implements MatchService {
    private MatchRepository matchRepository;

    @Override
    public void saveMatch(Match match) {
        matchRepository.save(match);
    }

    @Override
    public void saveMatches(List<Match> matches) {
      //  matches = matches.stream().filter(match -> !matchRepository.existsById(match.getId()))
        //        .collect(Collectors.toList());
        matchRepository.saveAll(matches);
    }

    // make a specific get todays matches method, this one gets all matches in db
    @Override
    @Cacheable(value = "footballDataCache", key = "'todayMatches'")
    // remember to make an update method with @CachePut (???)
    public List<Match> getMatches() {
        return matchRepository.findAll();
    }

    @Override
    @Cacheable(value = "footballDataCache", key = "'matchesByCompetitionId-' + #id")
    // beware of what's cached when this method is called but the database is empty (hint: it caches an empty array)
    // also remember to invalid cache according to the cron update
    public List<Match> getByCompetitionId(Integer id) {
        return matchRepository.findByCompetitionId(id);
    }
}
