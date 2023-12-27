package com.example.betkickapi.controller;

import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.User;
import com.example.betkickapi.service.bet.BetService;
import com.example.betkickapi.service.match.MatchService;
import com.example.betkickapi.service.user.UserService;
import com.example.betkickapi.web.internal.BetHistoryResponse;
import com.example.betkickapi.web.internal.BetRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@Slf4j
@Validated
public class BetController {
    private BetService betService;
    private UserService userService;
    private MatchService matchService;

    @PostMapping("/api/bet")
    public ResponseEntity<Double> makeBets(@RequestBody @Valid List<BetRequest> bets, @RequestParam @NotNull String userId) {
        log.info("Request to make a bet received!");
        bets.forEach(System.out::println);
        List<Integer> matchIds = bets.stream().map(BetRequest::getMatchId).toList();
        List<Match> matches = matchService.getNonFinishedMatchesByIds(matchIds);
        User user = userService.findById(userId);
        // returns current account balance
        return ResponseEntity.ok(betService.saveBets(bets, user, matches));
    }

    @GetMapping("/api/user/bets")
    public ResponseEntity<List<BetHistoryResponse>> makeBets(@RequestParam @NotNull String userId) {
        log.info("Request to retrieve bet history received!");
        List<BetHistoryResponse> response = betService.findBetsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/user/bet")
    @Transactional
    public ResponseEntity<Double> cancelBet(Integer betId) {
        log.info("Request to cancel a bet received!");
        return ResponseEntity.ok(betService.cancelBet(betId));
    }

}