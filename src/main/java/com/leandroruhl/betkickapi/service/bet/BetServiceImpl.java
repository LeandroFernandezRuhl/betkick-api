package com.leandroruhl.betkickapi.service.bet;

import com.leandroruhl.betkickapi.dto.internal_api.BetHistoryResponse;
import com.leandroruhl.betkickapi.dto.internal_api.BetRequest;
import com.leandroruhl.betkickapi.exception.EntityNotFoundException;
import com.leandroruhl.betkickapi.model.Bet;
import com.leandroruhl.betkickapi.model.Match;
import com.leandroruhl.betkickapi.model.User;
import com.leandroruhl.betkickapi.model.embbeded.MatchOdds;
import com.leandroruhl.betkickapi.model.enums.Status;
import com.leandroruhl.betkickapi.model.enums.Winner;
import com.leandroruhl.betkickapi.repository.BetRepository;
import com.leandroruhl.betkickapi.service.user.UserService;
import com.leandroruhl.betkickapi.service.utility.CacheService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The {@code BetServiceImpl} class implements the {@link BetService} interface
 * and provides concrete implementations for managing operations related to bets.
 * This class utilizes a {@link BetRepository} for interacting with bet data,
 * a {@link UserService} for user-related operations, a {@link ModelMapper} for entity mapping,
 * and a {@link CacheService} for cache-related operations.
 */
@Service
@AllArgsConstructor
public class BetServiceImpl implements BetService {

    private final BetRepository betRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final CacheService cacheService;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation finishes bets based on the outcome of a finished match.
     * It updates the status and winner information for each bet, and deposits winnings for successful bets.
     * The method also invalidates the leaderboard cache as it may have changed.
     *
     * @param finishedMatch The {@link Match} object representing the finished match.
     * @see Bet
     * @see UserService#deposit(User, Double)
     * @see BetRepository#saveAll(Iterable)
     */
    @Override
    public void finishBets(Match finishedMatch) {
        List<Bet> betsToFinish = betRepository.findByMatchId(finishedMatch.getId());
        if (!betsToFinish.isEmpty()) {
            betsToFinish.forEach(bet -> {
                bet.setMatch(finishedMatch);
                // If the user correctly guessed the winner of the match, they won the bet
                bet.setIsWon(finishedMatch.getWinner() == bet.getWinner());
                if (bet.getIsWon()) {
                    userService.deposit(bet.getUser(), bet.getAmount() * bet.getOdds());
                }
            });
            betRepository.saveAll(betsToFinish);
            // After bets are paid, the leaderboard may have changed
            cacheService.invalidateCacheForKey("leaderboard");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation creates finished bets for a list of bets using the {@link BetRepository#saveAll(Iterable)} method.
     *
     * @param bets The list of {@link Bet} objects to create finished bets for.
     */
    @Override
    public void createFinishedBets(List<Bet> bets) {
        betRepository.saveAll(bets);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation cancels a bet based on the bet ID, refunds the amount, and deletes the bet.
     * It throws an exception if the match has started, finished, or is awarded.
     *
     * @param betId The unique identifier of the bet.
     * @return The amount canceled for the canceled bet.
     * @throws IllegalStateException If the match has started, finished, or is awarded.
     * @see Bet
     * @see UserService#deposit(User, Double)
     * @see BetRepository#delete(Object)
     */
    @Override
    public Double cancelBet(Integer betId) {
        Bet bet = betRepository.findById(betId)
                .orElseThrow(() -> new EntityNotFoundException(Bet.class, "betId", betId.toString()));
        Status matchStatus = bet.getMatch().getStatus();
        if (matchStatus == Status.IN_PLAY || matchStatus == Status.PAUSED ||
                matchStatus == Status.FINISHED || matchStatus == Status.AWARDED) {
            throw new IllegalStateException("Can't cancel a bet if the match has started, finished, or is awarded");
        }
        User user = userService.deposit(bet.getUser(), bet.getAmount());
        betRepository.delete(bet);
        return user.getAccountBalance();
    }

    /**
     * This implementation finds bets by user ID and returns a list of bet history responses.
     * It utilizes the {@link BetRepository#findByUserId(UUID)} method and converts each bet to a DTO.
     *
     * @param userId The unique identifier of the user.
     * @return A list of {@link BetHistoryResponse} objects representing the bet history for the user.
     */
    @Override
    public List<BetHistoryResponse> findBetsByUserId(String userId) {
        List<Bet> bets = betRepository.findByUserId(UUID.fromString(userId));
        return bets.stream()
                .map(this::convertBetToDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts a {@link Bet} entity to a {@link BetHistoryResponse} DTO.
     *
     * @param bet The {@link Bet} object to be converted.
     * @return The corresponding {@link BetHistoryResponse} DTO.
     * @see ModelMapper
     */
    private BetHistoryResponse convertBetToDto(Bet bet) {
        return modelMapper.map(bet, BetHistoryResponse.class);
    }

    /**
     * This implementation saves bets for a user based on the provided bet requests and associated matches.
     * It filters out invalid bet requests, converts them to bet entities, withdraws the required amount from the user,
     * saves the bets, and returns the new account balance.
     *
     * @param betRequests The list of {@link BetRequest} objects representing the user's bet requests.
     * @param user        The {@link User} object for whom the bets are being saved.
     * @param matches     The list of {@link Match} objects associated with the bet requests.
     * @return The total amount of the saved bets.
     * @see UserService#withdraw(User, Double)
     * @see BetRepository#saveAll(Iterable)
     */
    @Override
    public Double saveBets(List<BetRequest> betRequests, User user, List<Match> matches) {
        // Create a matches by id map
        Map<Integer, Match> matchesById = matches
                .stream()
                .collect(Collectors.toMap(Match::getId, match -> match));

        // Filter out bet requests that don't have a valid match id
        // then convert those with a valid match id to bet entities
        List<Bet> bets = betRequests.stream()
                .filter(request -> matchesById.get(request.getMatchId()) != null)
                .map(request -> convertBetRequestToBet(request, user, matchesById.get(request.getMatchId())))
                .toList();

        // Update account balance; if funds are not sufficient, an exception is thrown
        Double newAccountBalance = userService
                .withdraw(user, bets.stream().mapToDouble(Bet::getAmount).reduce(0, Double::sum))
                .getAccountBalance();

        // If this line is reached, the funds are sufficient, so the bets are valid and can be saved
        betRepository.saveAll(bets);
        return newAccountBalance;
    }

    /**
     * Converts a {@link BetRequest} object to a {@link Bet} entity.
     *
     * @param betRequest The {@link BetRequest} object to be converted.
     * @param user       The {@link User} object associated with the bet.
     * @param match      The {@link Match} object associated with the bet.
     * @return The corresponding {@link Bet} entity.
     */
    private Bet convertBetRequestToBet(BetRequest betRequest, User user, Match match) {
        Bet bet = new Bet();
        bet.setAmount(betRequest.getBetAmount());
        bet.setWinner(betRequest.getWinner());

        // You can't trust that the odds in the request are the same as the ones in the database
        MatchOdds currentMatchOdds = match.getOdds();
        if (bet.getWinner() == Winner.AWAY_TEAM) {
            bet.setOdds(currentMatchOdds.getAwayWinsOdds());
        } else if (bet.getWinner() == Winner.HOME_TEAM) {
            bet.setOdds(currentMatchOdds.getHomeWinsOdds());
        } else {
            bet.setOdds(currentMatchOdds.getDrawOdds());
        }

        Instant currentInstant = Instant.now();
        LocalDateTime currentDate = LocalDateTime.ofInstant(currentInstant, ZoneOffset.UTC);
        bet.setPlacedAt(currentDate);

        bet.setUser(user);
        bet.setMatch(match);

        return bet;
    }
}
