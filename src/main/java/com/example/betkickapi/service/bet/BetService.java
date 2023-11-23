package com.example.betkickapi.service.bet;

import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.User;
import com.example.betkickapi.request.BetRequest;
import com.example.betkickapi.response.BetHistoryResponse;

import java.util.List;

public interface BetService {

    void finishBets(Match finishedMatch);

    List<BetHistoryResponse> findBetsByUserId(String userId);

    Double saveBets(List<BetRequest> betRequests, User user, List<Match> matches);
}
