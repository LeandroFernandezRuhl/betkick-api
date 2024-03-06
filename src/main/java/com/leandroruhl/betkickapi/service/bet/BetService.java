package com.leandroruhl.betkickapi.service.bet;

import com.leandroruhl.betkickapi.dto.internal_api.BetHistoryResponse;
import com.leandroruhl.betkickapi.dto.internal_api.BetRequest;
import com.leandroruhl.betkickapi.model.Bet;
import com.leandroruhl.betkickapi.model.Match;
import com.leandroruhl.betkickapi.model.User;

import java.util.List;

/**
 * The BetService interface provides methods for managing operations related to bets.
 * Implementations of this interface handle functionalities such as finishing bets based on a finished match,
 * creating finished bets, canceling a bet, finding bets by user ID, etc.
 */
public interface BetService {

    /**
     * Finishes bets based on the outcome of a finished match.
     *
     * @param finishedMatch The {@link Match} object representing the finished match.
     * @see Bet
     */
    void finishBets(Match finishedMatch);

    /**
     * Creates finished bets for a list of bets.
     *
     * @param bets The list of {@link Bet} objects to create finished bets for.
     */
    void createFinishedBets(List<Bet> bets);

    /**
     * Cancels a bet based on the bet ID and returns the amount that was canceled.
     *
     * @param betId The unique identifier of the bet.
     * @return The amount canceled for the canceled bet.
     * @see Bet
     */
    Double cancelBet(Integer betId);

    /**
     * Finds bets by user ID and returns a list of bet history responses.
     *
     * @param userId The unique identifier of the user.
     * @return A list of {@link BetHistoryResponse} objects representing the bet history for the user.
     */
    List<BetHistoryResponse> findBetsByUserId(String userId);

    /**
     * Saves bets for a user based on the provided bet requests and associated matches.
     *
     * @param betRequests The list of {@link BetRequest} objects representing the user's bet requests.
     * @param user        The {@link User} object for whom the bets are being saved.
     * @param matches     The list of {@link Match} objects associated with the bet requests.
     * @return The total amount of the saved bets.
     */
    Double saveBets(List<BetRequest> betRequests, User user, List<Match> matches);
}
