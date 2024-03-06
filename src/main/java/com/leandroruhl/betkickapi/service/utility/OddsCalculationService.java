package com.leandroruhl.betkickapi.service.utility;

import com.leandroruhl.betkickapi.dto.external_api.HeadToHeadResponse;
import com.leandroruhl.betkickapi.dto.external_api.TeamStatsResponse;
import com.leandroruhl.betkickapi.model.Standing;
import com.leandroruhl.betkickapi.model.embbeded.MatchOdds;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * OddsCalculationService class handles the calculation of match odds based on various factors such as team statistics,
 * head-to-head performance, and standings. It provides methods to generate match odds and calculate probabilities for
 * different outcomes.
 * <br>
 * <br>
 * This class uses weights and rates to determine the influence of different factors on the final match odds calculation.
 * The weights include team rates, recent team rates, head-to-head rates, recent head-to-head rates, standing rates,
 * and standing position weight.
 * <br>
 * <br>
 * The calculation involves considering team statistics, recent performance, head-to-head history, and standings to
 * estimate the probabilities of home win, away win, and draw. These probabilities are then normalized and adjusted to
 * ensure a meaningful representation of match odds.
 * <br>
 * <br>
 * Assumptions:
 * - The input data provided (TeamStatsResponse, HeadToHeadResponse, List<Standing>) is assumed to be valid and
 * representative of the teams' performance and standings.
 * - Certain conditions, such as the total number of matches being non-zero, are assumed for meaningful rate calculations.
 * - The weights and algorithms used for probability and odds calculation are based on empirical considerations and may
 * be subject to further fine-tuning.
 */
@Service
@Slf4j
public class OddsCalculationService {

    private final Double teamRatesWeight = 0.3; // Rate over the last 2 years
    private final Double recentTeamRatesWeight = 0.15; // Rate over the past 5 matches
    private final Double h2hRatesWeight = 0.01; // Historical H2H rate
    private final Double recentH2hRatesWeight = 0.04; // Rate over the last 5 H2H matches
    private final Double standingRatesWeight = 0.05; // Stats of a team in the current competition (W, L, D, GF, GA, etc.)
    private final Double standingPositionWeight = 0.45; // Position of the team in the current competition's table

    private enum TeamRates {
        WIN_RATE, DRAW_RATE,
        RECENT_WIN_RATE, RECENT_DRAW_RATE,
    }

    private enum StandingRates {
        COMP_WIN_RATE, COMP_DRAW_RATE, COMP_POSITION
    }

    private enum HeadToHeadRates {
        H2H_AWAY_WIN_RATE, H2H_HOME_WIN_RATE, H2H_DRAW_RATE,
        RECENT_H2H_AWAY_WIN_RATE, RECENT_H2H_HOME_WIN_RATE, RECENT_H2H_DRAW_RATE,
    }

    /**
     * Calculates and returns a HashMap of various team rates based on the provided {@link TeamStatsResponse}.
     *
     * @param teamStats The {@link TeamStatsResponse} containing information about the team's performance.
     * @return A HashMap with keys representing different team rates and values as their calculated rates.
     * Returns null if total or recent team match counts are zero.
     */
    private HashMap<TeamRates, Double> calculateTeamRates(TeamStatsResponse teamStats) {
        Double draws = teamStats.getDraws();
        Double wins = teamStats.getWins();
        Double losses = teamStats.getLosses();
        Double totalMatches = losses + wins + draws;

        Double recentDraws = teamStats.getRecentDraws();
        Double recentWins = teamStats.getRecentWins();
        Double recentLosses = teamStats.getRecentLosses();
        Double recentMatches = recentLosses + recentWins + recentDraws;


        if (totalMatches == 0 || recentMatches == 0) {
            log.warn("Count of total or recent team matches is 0");
            return null;
        }

        HashMap<TeamRates, Double> teamRates = new HashMap<>();
        teamRates.put(TeamRates.WIN_RATE, wins / totalMatches);
        teamRates.put(TeamRates.DRAW_RATE, draws / totalMatches);
        teamRates.put(TeamRates.RECENT_WIN_RATE, recentWins / recentMatches);
        teamRates.put(TeamRates.RECENT_DRAW_RATE, recentDraws / recentMatches);
        return teamRates;
    }

    /**
     * Calculates and returns a HashMap of standing rates based on the provided {@link Standing} and totalTeams.
     *
     * @param standing   The {@link Standing} object containing information about the team's standing in a competition.
     * @param totalTeams The total number of teams in the competition.
     * @return A HashMap with keys representing different standing rates and values as their calculated rates.
     */
    private HashMap<StandingRates, Double> calculateStandingRates(Standing standing, Integer totalTeams) {
        Double draws = Double.valueOf(standing.getDraw());
        Double wins = Double.valueOf(standing.getWon());
        Double losses = Double.valueOf(standing.getLost());
        Double totalMatches = losses + wins + draws;
        Double positionNormalized = 1D - (double) (standing.getPosition() - 1) / totalTeams;

        HashMap<StandingRates, Double> standingRates = new HashMap<>();
        standingRates.put(StandingRates.COMP_WIN_RATE, wins / totalMatches);
        standingRates.put(StandingRates.COMP_DRAW_RATE, draws / totalMatches);
        standingRates.put(StandingRates.COMP_POSITION, positionNormalized);
        return standingRates;
    }

    /**
     * Determines if the given list of standings contains meaningful data for statistical analysis.
     *
     * @param standings The list of {@link Standing} objects representing the team standings.
     * @return true if standings are considered valid; false otherwise.
     */
    private boolean validateStandings(List<Standing> standings) {
        boolean standingsAreValid = true;

        for (Standing standing : standings) {
            if (standing.getCompetition().getGroup() != null) // This means the comp is at a group stage
                standingsAreValid = standing.getDraw() + standing.getWon() + standing.getLost() >= 3;
            else // The competition has a league format so more matches are needed for stats to be meaningful
                standingsAreValid = standing.getDraw() + standing.getWon() + standing.getLost() >= 10;

        }

        return standingsAreValid;
    }

    /**
     * Calculates and returns a HashMap of head-to-head rates based on the provided {@link HeadToHeadResponse}.
     *
     * @param h2h The {@link HeadToHeadResponse} containing information about the teams' historical performance.
     * @return A HashMap with keys representing different head-to-head rates and values as their calculated rates.
     * Returns null if total head-to-head match count is zero.
     */
    private HashMap<HeadToHeadRates, Double> calculateHeadToHeadRates(HeadToHeadResponse h2h) {

        if (h2h.getAwayTeamStats() == null || h2h.getHomeTeamStats() == null) {
            return null;
        }

        Double totalMatches = h2h.getNumberOfMatches();
        Double drawRate = h2h.getAwayTeamStats().getDraws() / totalMatches;
        Double awayWinRate = h2h.getAwayTeamStats().getWins() / totalMatches;
        Double homeWinRate = h2h.getHomeTeamStats().getWins() / totalMatches;

        Double recentDraws = h2h.getAwayTeamStats().getRecentDraws();
        Double recentAwayWins = h2h.getAwayTeamStats().getRecentWins();
        Double recentHomeWins = h2h.getHomeTeamStats().getRecentWins();
        Double recentMatches = recentDraws + recentAwayWins + recentHomeWins;

        if (totalMatches == 0) {
            log.warn("Count of total head to head matches is 0");
            return null;
        }

        HashMap<HeadToHeadRates, Double> h2hRates = new HashMap<>();
        h2hRates.put(HeadToHeadRates.H2H_DRAW_RATE, drawRate);
        h2hRates.put(HeadToHeadRates.H2H_AWAY_WIN_RATE, awayWinRate);
        h2hRates.put(HeadToHeadRates.H2H_HOME_WIN_RATE, homeWinRate);

        if (recentMatches != 0) {
            h2hRates.put(HeadToHeadRates.RECENT_H2H_DRAW_RATE, recentDraws / recentMatches);
            h2hRates.put(HeadToHeadRates.RECENT_H2H_AWAY_WIN_RATE, recentAwayWins / recentMatches);
            h2hRates.put(HeadToHeadRates.RECENT_H2H_HOME_WIN_RATE, recentHomeWins / recentMatches);
        }

        return h2hRates;
    }

    /**
     * Generates and returns {@link MatchOdds} based on the provided team statistics, head-to-head data, standings, and team IDs.
     *
     * @param home       The {@link TeamStatsResponse} for the home team.
     * @param away       The {@link TeamStatsResponse} for the away team.
     * @param h2h        The {@link HeadToHeadResponse} containing head-to-head data.
     * @param standings  The list of {@link Standing} objects representing current standings.
     * @param homeId     The ID of the home team.
     * @param totalTeams The total number of teams in the competition.
     * @return {@link MatchOdds} representing the calculated odds for home win, away win, and draw.
     * Returns null if any of the required input data is invalid.
     */
    public MatchOdds generateMatchOdds(TeamStatsResponse home, TeamStatsResponse away, HeadToHeadResponse h2h,
                                       List<Standing> standings, Integer homeId, Integer totalTeams) {
        HashMap<TeamRates, Double> homeRates = this.calculateTeamRates(home);
        HashMap<TeamRates, Double> awayRates = this.calculateTeamRates(away);
        HashMap<HeadToHeadRates, Double> h2hRates = this.calculateHeadToHeadRates(h2h);

        if (homeRates == null || awayRates == null || h2hRates == null) {
            return null;
        }

        HashMap<StandingRates, Double> homeStandingRates = new HashMap<>();
        HashMap<StandingRates, Double> awayStandingRates = new HashMap<>();

        if (standings != null && this.validateStandings(standings)) {
            Standing homeStanding = null;
            Standing awayStanding = null;

            for (Standing standing : standings) {
                if (standing.getTeam().getId() == homeId)
                    homeStanding = standing;
                else
                    awayStanding = standing;

            }

            homeStandingRates = this.calculateStandingRates(homeStanding, totalTeams);
            awayStandingRates = this.calculateStandingRates(awayStanding, totalTeams);
        }

        double homeWinProbability = calculateWinProbability(
                homeRates.get(TeamRates.WIN_RATE), homeRates.get(TeamRates.RECENT_WIN_RATE),
                h2hRates.get(HeadToHeadRates.H2H_HOME_WIN_RATE), h2hRates.get(HeadToHeadRates.RECENT_H2H_HOME_WIN_RATE),
                homeStandingRates.get(StandingRates.COMP_WIN_RATE), homeStandingRates.get(StandingRates.COMP_POSITION));

        double awayWinProbability = calculateWinProbability(
                awayRates.get(TeamRates.WIN_RATE), awayRates.get(TeamRates.RECENT_WIN_RATE),
                h2hRates.get(HeadToHeadRates.H2H_AWAY_WIN_RATE), h2hRates.get(HeadToHeadRates.RECENT_H2H_AWAY_WIN_RATE),
                awayStandingRates.get(StandingRates.COMP_WIN_RATE), awayStandingRates.get(StandingRates.COMP_POSITION));


        double drawProbability = calculateDrawProbability(
                homeRates.get(TeamRates.DRAW_RATE), awayRates.get(TeamRates.DRAW_RATE),
                homeRates.get(TeamRates.RECENT_DRAW_RATE), awayRates.get(TeamRates.RECENT_DRAW_RATE),
                h2hRates.get(HeadToHeadRates.H2H_DRAW_RATE), h2hRates.get(HeadToHeadRates.RECENT_H2H_DRAW_RATE),
                homeStandingRates.get(StandingRates.COMP_DRAW_RATE), awayStandingRates.get(StandingRates.COMP_DRAW_RATE));


        // Normalize probabilities
        double totalProbability = homeWinProbability + awayWinProbability + drawProbability;
        homeWinProbability /= totalProbability;
        awayWinProbability /= totalProbability;
        drawProbability /= totalProbability;
        // Adjust draw probability
        homeWinProbability -= 0.0175;
        awayWinProbability -= 0.0175;
        drawProbability += 0.035;


        Double homeWinOdds = calculateOdds(homeWinProbability, false);
        Double awayWinOdds = calculateOdds(awayWinProbability, false);
        Double drawOdds = calculateOdds(drawProbability, true);

        return new MatchOdds(awayWinOdds, homeWinOdds, drawOdds, false);
    }

    /**
     * Calculates and returns odds based on the provided probability and adjusts them for specific conditions.
     *
     * @param probability The calculated probability for a specific outcome.
     * @param isDraw      A boolean indicating if the outcome is a draw.
     * @return Double representing the calculated odds.
     */
    private double calculateOdds(Double probability, boolean isDraw) {
        double odds = 1D / probability;
        if (odds <= 1D)
            return Math.max(odds / 2D + 1D, 1.1);
        if (isDraw && odds > 5) {
            double difference = odds - 5D;
            return 5 + difference * 0.25;
        }
        return odds;
    }

    /**
     * Calculates and returns the win probability of a team based on various team and standing rates.
     *
     * @param teamWinRate       The overall win rate for the team.
     * @param recentTeamWinRate The recent win rate for the team.
     * @param h2hWinRate        The historical head-to-head win rate.
     * @param recentH2hWinRate  The recent head-to-head win rate.
     * @param standingWinRate   The standing win rate.
     * @param standingPosition  The normalized standing position.
     * @return Double representing the calculated win probability.
     */
    private Double calculateWinProbability(Double teamWinRate, Double recentTeamWinRate, Double h2hWinRate,
                                           Double recentH2hWinRate, Double standingWinRate, Double standingPosition) {
        // H2hWinRate is historical so there is always going to be some data or be null,
        // however this means that h2hWinRate can simultaneously not be null while having no recent matches
        if (standingWinRate == null && recentH2hWinRate == null) {
            log.info("No standing and no recent H2H win rate");
            // if there is no standing data and no recent h2h matches, redistribute that weight equally
            return teamWinRate * (teamRatesWeight + recentH2hRatesWeight / 3D + standingRatesWeight / 3D + standingPositionWeight / 3D) +
                    recentTeamWinRate * (recentTeamRatesWeight + recentH2hRatesWeight / 3D + standingRatesWeight / 3D + standingPositionWeight / 3D) +
                    h2hWinRate * (h2hRatesWeight + recentH2hRatesWeight / 3D + standingRatesWeight / 3D + standingPositionWeight / 3D);
        }

        if (recentH2hWinRate == null) {
            // If there are no recent h2h matches, redistribute that weight equally
            log.info("No recent H2H win rate");
            return teamWinRate * (teamRatesWeight + recentH2hRatesWeight / 5D) +
                    recentTeamWinRate * (recentTeamRatesWeight + recentH2hRatesWeight / 5D) +
                    h2hWinRate * (h2hRatesWeight + recentH2hRatesWeight / 5D) +
                    standingWinRate * (standingRatesWeight + recentH2hRatesWeight / 5D) +
                    standingPosition * (standingPositionWeight + recentH2hRatesWeight / 5D);
        }

        if (standingWinRate == null) {
            // If there is no standing data, redistribute that weight equally
            log.info("No standing win rate");
            return teamWinRate * (teamRatesWeight + standingRatesWeight / 4D + standingPositionWeight / 4D) +
                    recentTeamWinRate * (recentTeamRatesWeight + standingRatesWeight / 4D + standingPositionWeight / 4D) +
                    h2hWinRate * (h2hRatesWeight + standingRatesWeight / 4D + standingPositionWeight / 4D) +
                    recentH2hWinRate * (recentH2hRatesWeight + standingRatesWeight / 4D + standingPositionWeight / 4D);
        }

        log.info("Win rate calculated successfully");
        return teamWinRate * teamRatesWeight + recentTeamWinRate * recentTeamRatesWeight +
                h2hWinRate * h2hRatesWeight + recentH2hWinRate * recentH2hRatesWeight +
                standingWinRate * standingRatesWeight + standingPosition * standingPositionWeight;
    }

    /**
     * Calculates and returns the draw probability based on various draw rates and standing draw rate.
     *
     * @param homeDrawRate         The overall draw rate for the home team.
     * @param awayDrawRate         The overall draw rate for the away team.
     * @param recentHomeDrawRate   The recent draw rate for the home team.
     * @param recentAwayDrawRate   The recent draw rate for the away team.
     * @param h2hDrawRate          The historical head-to-head draw rate.
     * @param recentH2hDrawRate    The recent head-to-head draw rate.
     * @param homeStandingDrawRate The standing draw rate for the home team.
     * @param awayStandingDrawRate The standing draw rate for the away team.
     * @return Double representing the calculated draw probability.
     */
    private Double calculateDrawProbability(Double homeDrawRate, Double awayDrawRate, Double recentHomeDrawRate,
                                            Double recentAwayDrawRate, Double h2hDrawRate, Double recentH2hDrawRate,
                                            Double homeStandingDrawRate, Double awayStandingDrawRate) {
        double teamsDrawAvg = (homeDrawRate + awayDrawRate) / 2D;
        double recentTeamsDrawAvg = (recentHomeDrawRate + recentAwayDrawRate) / 2D;

        if (homeStandingDrawRate == null && recentH2hDrawRate == null) {
            log.info("No standing and no recent H2H draw rate");
            return teamsDrawAvg * (teamRatesWeight + recentH2hRatesWeight / 3D + standingRatesWeight / 3D + standingPositionWeight / 3D) +
                    recentTeamsDrawAvg * (recentTeamRatesWeight + recentH2hRatesWeight / 3D + standingRatesWeight / 3D + standingPositionWeight / 3D) +
                    h2hDrawRate * (h2hRatesWeight + recentH2hRatesWeight / 3D + standingRatesWeight / 3D + standingPositionWeight / 3D);
        }

        if (homeStandingDrawRate == null) {
            log.info("No standing draw rate");
            return teamsDrawAvg * (teamRatesWeight + standingRatesWeight / 4D + standingPositionWeight / 4D) +
                    recentTeamsDrawAvg * (recentTeamRatesWeight + standingRatesWeight / 4D + standingPositionWeight / 4D) +
                    h2hDrawRate * (h2hRatesWeight + standingRatesWeight / 4D + standingPositionWeight / 4D) +
                    recentH2hDrawRate * (recentH2hRatesWeight + standingRatesWeight / 4D + standingPositionWeight / 4D);
        }

        double teamsStandingsDrawAvg = (homeStandingDrawRate + awayStandingDrawRate) / 2D;

        if (recentH2hDrawRate == null) {
            log.info("No recent H2H draw rate");
            return teamsDrawAvg * (teamRatesWeight + recentH2hRatesWeight / 4D + standingPositionWeight / 4D) +
                    recentTeamsDrawAvg * (recentTeamRatesWeight + recentH2hRatesWeight / 4D + standingPositionWeight / 4D) +
                    h2hDrawRate * (h2hRatesWeight + recentH2hRatesWeight / 4D + standingPositionWeight / 4D) +
                    teamsStandingsDrawAvg * (standingRatesWeight + recentH2hRatesWeight / 4D + standingPositionWeight / 4D);
        }

        log.info("Draw rate calculated successfully");
        return teamsDrawAvg * (teamRatesWeight + standingPositionWeight / 5D) +
                recentTeamsDrawAvg * (recentTeamRatesWeight + standingPositionWeight / 5D) +
                h2hDrawRate * (h2hRatesWeight + standingPositionWeight / 5D) +
                recentH2hDrawRate * (recentH2hRatesWeight + standingPositionWeight / 5D) +
                teamsStandingsDrawAvg * (standingRatesWeight + standingPositionWeight / 5D);
    }
}
