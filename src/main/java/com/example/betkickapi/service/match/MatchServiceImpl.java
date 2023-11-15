package com.example.betkickapi.service.match;

import com.example.betkickapi.model.Match;
import com.example.betkickapi.repository.MatchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    @Cacheable(value = "footballDataCache", key = "'allMatches'")
    // remember to make an update method with @CachePut (???)
    public List<Match> getMatches() {
        return matchRepository.findAll();
    }
}
