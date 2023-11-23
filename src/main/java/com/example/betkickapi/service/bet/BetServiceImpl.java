package com.example.betkickapi.service.bet;

import com.example.betkickapi.model.Bet;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.User;
import com.example.betkickapi.repository.BetRepository;
import com.example.betkickapi.request.BetRequest;
import com.example.betkickapi.response.BetHistoryResponse;
import com.example.betkickapi.service.user.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BetServiceImpl implements BetService {
    private BetRepository betRepository;
    private UserService userService;
    private ModelMapper modelMapper;

    @Override
    public void finishBets(Match finishedMatch) {
        List<Bet> betsToFinish = betRepository.findByMatchId(finishedMatch.getId());
        betsToFinish.forEach(bet -> {
            bet.setMatch(finishedMatch);
            // if the user correctly guessed the winner of the match, he won the bot
            bet.setIsWon(finishedMatch.getWinner() == bet.getWinner());
            if (bet.getIsWon()) {
                userService.incrementUserBalance(bet.getUser(), bet.getAmount() * bet.getOdds());
            }
        });
        betRepository.saveAll(betsToFinish);
    }

    @Override
    public List<BetHistoryResponse> findBetsByUserId(String userId) {
        List<Bet> bets = betRepository.findByUserId(userId);
        return bets.stream()
                .map(this::convertBetToDto)
                .collect(Collectors.toList());
    }

    private BetHistoryResponse convertBetToDto(Bet bet) {
        return modelMapper.map(bet, BetHistoryResponse.class);
    }

    @Override
    public Double saveBets(List<BetRequest> betRequests, User user, List<Match> matches) {
        // create a matches by id map
        Map<Integer, Match> matchesById = matches
                .stream()
                .collect(Collectors.toMap(Match::getId, match -> match));

        // filter out bet requests that don't have a valid match id
        // then convert those with a valid match id to bet entities
        List<Bet> bets = betRequests.stream()
                .filter( request -> matchesById.get(request.getMatchId()) != null)
                .map(request -> convertBetRequestToBet(request, user, matchesById.get(request.getMatchId())))
                .toList();

        // update account balance, if funds are not sufficient an exception is thrown
        Double newAccountBalance = userService
                .decrementUserBalance(user, bets.stream().mapToDouble(Bet::getAmount).reduce(0, Double::sum))
                .getAccountBalance();

        // if this line is reached the funds are sufficient so the bets are valid and can be saved
        betRepository.saveAll(bets);
        return newAccountBalance;
    }

    private Bet convertBetRequestToBet(BetRequest betRequest, User user, Match match) {
        Bet bet = new Bet();
        bet.setOdds(betRequest.getBetOdds());
        bet.setAmount(betRequest.getBetAmount());
        bet.setWinner(betRequest.getWinner());

        Instant currentInstant = Instant.now();
        LocalDateTime currentDate = LocalDateTime.ofInstant(currentInstant, ZoneOffset.UTC);
        bet.setPlacedAt(currentDate);

        bet.setUser(user);
        bet.setMatch(match);

        return bet;
    }
}
