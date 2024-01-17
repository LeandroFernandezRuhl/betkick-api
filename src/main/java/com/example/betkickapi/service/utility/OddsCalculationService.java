package com.example.betkickapi.service.utility;

import com.example.betkickapi.model.Standing;
import com.example.betkickapi.model.embbeded.MatchOdds;
import com.example.betkickapi.web.externalApi.HeadToHeadResponse;
import com.example.betkickapi.web.externalApi.TeamStatsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;


@Service
@Slf4j
public class OddsCalculationService {
    private final Double teamRatesWeight = 0.3; // rate over the last 2 years
    private final Double recentTeamRatesWeight = 0.15; // rate over the past 5 matches
    private final Double h2hRatesWeight = 0.01; // historical H2H rate
    private final Double recentH2hRatesWeight = 0.04; // rate over the last 5 H2H matches
    private final Double standingRatesWeight = 0.05;
    private final Double standingPositionWeight = 0.45;

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

    // determines if the given standing stats are meaningful enough to be taken into account
    private boolean validateStandings(List<Standing> standings) {
        boolean standingsAreValid = true;

        for (Standing standing : standings) {
            if (standing.getCompetition().getGroup() != null) // this means the comp is at a group stage
                standingsAreValid = standing.getDraw() + standing.getWon() + standing.getLost() >= 3;
            else // the competition has a league format so more matches are needed for stats to be meaningful
                standingsAreValid = standing.getDraw() + standing.getWon() + standing.getLost() >= 10;

        }

        return standingsAreValid;
    }

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
        // adjust draw probability
        homeWinProbability -= 0.0175;
        awayWinProbability -= 0.0175;
        drawProbability += 0.035;
        log.info("HOME WIN PROBABILITY: " + homeWinProbability);
        log.info("DRAW PROBABILITY: " + drawProbability);
        log.info("AWAY WIN PROBABILITY: " + awayWinProbability);


        Double homeWinOdds = calculateOdds(homeWinProbability, false);
        Double awayWinOdds = calculateOdds(awayWinProbability, false);
        Double drawOdds = calculateOdds(drawProbability, true);
        log.info("HOME ODDS: " + homeWinOdds);
        log.info("DRAW ODDS: " + drawOdds);
        log.info("AWAY ODDS: " + awayWinOdds);

        return new MatchOdds(awayWinOdds, homeWinOdds, drawOdds, false);
    }

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

    private Double calculateWinProbability(Double teamWinRate, Double recentTeamWinRate, Double h2hWinRate,
                                           Double recentH2hWinRate, Double standingWinRate, Double standingPosition) {
        // h2hWinRate is historical so there is always going to be some data or be null,
        // however this means that h2hWinRate can simultaneously not be null while having no recent matches
        if (standingWinRate == null && recentH2hWinRate == null) {
            log.info("No standing and no recent H2H win rate");
            // if there is no standing data and no recent h2h matches, redistribute that weight equally
            return teamWinRate * (teamRatesWeight + recentH2hRatesWeight / 3D + standingRatesWeight / 3D + standingPositionWeight / 3D) +
                    recentTeamWinRate * (recentTeamRatesWeight + recentH2hRatesWeight / 3D + standingRatesWeight / 3D + standingPositionWeight / 3D) +
                    h2hWinRate * (h2hRatesWeight + recentH2hRatesWeight / 3D + standingRatesWeight / 3D + standingPositionWeight / 3D);
        }

        if (recentH2hWinRate == null) {
            // if there are no recent h2h matches, redistribute that weight equally
            log.info("No recent H2H win rate");
            return teamWinRate * (teamRatesWeight + recentH2hRatesWeight / 5D) +
                    recentTeamWinRate * (recentTeamRatesWeight + recentH2hRatesWeight / 5D) +
                    h2hWinRate * (h2hRatesWeight + recentH2hRatesWeight / 5D) +
                    standingWinRate * (standingRatesWeight + recentH2hRatesWeight / 5D) +
                    standingPosition * (standingPositionWeight + recentH2hRatesWeight / 5D);
        }

        if (standingWinRate == null) {
            // if there is no standing data, redistribute that weight equally
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

    private Double calculateDrawProbability(Double homeDrawRate, Double awayDrawRate, Double recentHomeDrawRate,
                                            Double recentAwayDrawRate, Double h2hDrawRate, Double recentH2hDrawRate,
                                            Double homeStandingDrawRate, Double awayStandingDrawRate) {
        double teamsDrawAvg = (homeDrawRate + awayDrawRate) / 2D;
        double recentTeamsDrawAvg = (recentHomeDrawRate + recentAwayDrawRate) / 2D;

        if (homeStandingDrawRate == null && recentH2hDrawRate == null ) {
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
