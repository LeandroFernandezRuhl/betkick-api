package com.example.betkickapi.service.bet;

import com.example.betkickapi.model.Bet;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.User;
import com.example.betkickapi.web.internal.BetHistoryResponse;
import com.example.betkickapi.web.internal.BetRequest;

import java.util.List;

public interface BetService {

    void finishBets(Match finishedMatch);

    void createFinishedBets(List<Bet> bets);

    Double cancelBet(Integer betId);

    List<BetHistoryResponse> findBetsByUserId(String userId);

    Double saveBets(List<BetRequest> betRequests, User user, List<Match> matches);
}
