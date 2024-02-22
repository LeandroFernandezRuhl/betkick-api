package com.example.betkickapi.controller;

import com.example.betkickapi.dto.internal_api.BetHistoryResponse;
import com.example.betkickapi.dto.internal_api.BetRequest;
import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.User;
import com.example.betkickapi.service.bet.BetService;
import com.example.betkickapi.service.match.MatchService;
import com.example.betkickapi.service.user.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The BetController class is a Spring Web MVC controller responsible for handling bet-related endpoints,
 * including making bets, retrieving user bet history, and canceling bets.
 */
@AllArgsConstructor
@RestController
@Slf4j
@Validated
public class BetController {

    private final BetService betService;
    private final UserService userService;
    private final MatchService matchService;

    /**
     * Processes a request to make bets based on the provided list of {@link BetRequest}.
     *
     * @param bets   The list of bet requests.
     * @param userId The user ID associated with the bets.
     * @return A {@link ResponseEntity} containing the updated account balance after making the bets.
     * @see BetService#saveBets(List, User, List)
     * @see MatchService#getNonFinishedMatchesByIds(List)
     */
    @PostMapping("/api/bet")
    public ResponseEntity<Double> makeBets(@RequestBody @Valid List<BetRequest> bets, @RequestParam @NotNull String userId) {
        log.info("Request to make a bet received!");
        bets.forEach(System.out::println);
        List<Integer> matchIds = bets.stream().map(BetRequest::getMatchId).toList();
        List<Match> matches = matchService.getNonFinishedMatchesByIds(matchIds);
        User user = userService.findById(userId);
        // Returns current account balance
        return ResponseEntity.ok(betService.saveBets(bets, user, matches));
    }

    /**
     * Retrieves the bet history for a specific user.
     *
     * @param userId The user ID for whom to retrieve the bet history.
     * @return A {@link ResponseEntity} containing a list of {@link BetHistoryResponse}.
     * @see BetService#findBetsByUserId(String)
     */
    @GetMapping("/api/user/bets")
    public ResponseEntity<List<BetHistoryResponse>> getUserBets(@RequestParam @NotNull String userId) {
        log.info("Request to retrieve bet history received!");
        List<BetHistoryResponse> response = betService.findBetsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels a bet based on the provided bet ID.
     *
     * @param betId The ID of the bet to be canceled.
     * @return A {@link ResponseEntity} containing the updated account balance after canceling the bet.
     * @see BetService#cancelBet(Integer)
     */
    @DeleteMapping("/api/user/bet")
    @Transactional
    public ResponseEntity<Double> cancelBet(Integer betId) {
        log.info("Request to cancel a bet received!");
        return ResponseEntity.ok(betService.cancelBet(betId));
    }
}