package com.example.betkickapi.service.utility;

import com.example.betkickapi.model.embbeded.MatchOdds;
import com.example.betkickapi.web.externalApi.HeadToHeadResponse;
import com.example.betkickapi.web.externalApi.TeamStatsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class OddsCalculationService {
    private final Double teamRatesWeight = 0.3;
    private final Double h2hRatesWeight = 0.01;
    private final Double recentTeamRatesWeight = 0.6;
    private final Double recentH2hRatesWeight = 0.09;

    private enum TeamRates {
        WIN_RATE, DRAW_RATE,
        RECENT_WIN_RATE, RECENT_DRAW_RATE,
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

    public MatchOdds generateMatchOdds(TeamStatsResponse home, TeamStatsResponse away, HeadToHeadResponse h2h) {
        HashMap<TeamRates, Double> homeRates = this.calculateTeamRates(home);
        HashMap<TeamRates, Double> awayRates = this.calculateTeamRates(away);
        HashMap<HeadToHeadRates, Double> h2hRates = this.calculateHeadToHeadRates(h2h);

        if (homeRates == null || awayRates == null || h2hRates == null) {
            return null;
        }

        double homeWinProbability = calculateWinProbability(
                homeRates.get(TeamRates.WIN_RATE), homeRates.get(TeamRates.RECENT_WIN_RATE),
                h2hRates.get(HeadToHeadRates.H2H_HOME_WIN_RATE), h2hRates.get(HeadToHeadRates.RECENT_H2H_HOME_WIN_RATE));

        double awayWinProbability = calculateWinProbability(
                awayRates.get(TeamRates.WIN_RATE), awayRates.get(TeamRates.RECENT_WIN_RATE),
                h2hRates.get(HeadToHeadRates.H2H_AWAY_WIN_RATE), h2hRates.get(HeadToHeadRates.RECENT_H2H_AWAY_WIN_RATE));

        double drawProbability = calculateDrawProbability(
                homeRates.get(TeamRates.DRAW_RATE), awayRates.get(TeamRates.DRAW_RATE),
                homeRates.get(TeamRates.RECENT_DRAW_RATE), awayRates.get(TeamRates.RECENT_DRAW_RATE),
                h2hRates.get(HeadToHeadRates.H2H_DRAW_RATE), h2hRates.get(HeadToHeadRates.RECENT_H2H_DRAW_RATE));

        // Normalize probabilities
        double totalProbability = homeWinProbability + awayWinProbability + drawProbability;
        homeWinProbability /= totalProbability;
        awayWinProbability /= totalProbability;
        drawProbability /= totalProbability;
        log.info("HOME WIN PROBABILITY: " + homeWinProbability);
        log.info("DRAW PROBABILITY: " + drawProbability);
        log.info("AWAY WIN PROBABILITY: " + awayWinProbability);

        Double homeWinOdds = calculateOdds(homeWinProbability);
        Double awayWinOdds = calculateOdds(awayWinProbability);
        Double drawOdds = calculateOdds(drawProbability);
        log.info("HOME ODDS: " + homeWinOdds);
        log.info("DRAW ODDS: " + drawOdds);
        log.info("AWAY ODDS: " + awayWinOdds);

        return new MatchOdds(awayWinOdds, homeWinOdds, drawOdds, false);
    }

    private double calculateOdds(Double probability) {
        return 1D / probability;
    }

    private Double calculateWinProbability(Double teamWinRate, Double recentTeamWinRate,
                                           Double h2hWinRate, Double recentH2hWinRate) {
        if (recentH2hWinRate == null) {
            // if there are no recent h2h matches, redistribute that weight equally
            return teamWinRate * (teamRatesWeight + recentH2hRatesWeight / 3) + recentTeamWinRate * (recentTeamRatesWeight + recentH2hRatesWeight / 3) +
                    h2hWinRate * (h2hRatesWeight + recentH2hRatesWeight / 3);
        }

        return teamWinRate * teamRatesWeight + recentTeamWinRate * recentTeamRatesWeight +
                h2hWinRate * h2hRatesWeight + recentH2hWinRate * recentH2hRatesWeight;
    }

    private Double calculateDrawProbability(Double homeDrawRate, Double awayDrawRate,
                                            Double recentHomeDrawRate, Double recentAwayDrawRate,
                                            Double h2hDrawRate, Double recentH2hDrawRate) {
        Double teamsDrawAvg = (homeDrawRate + awayDrawRate) / 2;
        Double recentTeamsDrawAvg = (recentHomeDrawRate + recentAwayDrawRate) / 2;

        if (recentH2hDrawRate == null) {
            // if there are no recent h2h matches, redistribute that weight equally
            return teamsDrawAvg * (teamRatesWeight + recentH2hRatesWeight / 3) + recentTeamsDrawAvg * (recentTeamRatesWeight + recentH2hRatesWeight / 3) +
                    h2hDrawRate * (h2hRatesWeight + recentH2hRatesWeight / 3);
        }

        return teamsDrawAvg * teamRatesWeight + recentTeamsDrawAvg * recentTeamRatesWeight +
                h2hDrawRate * h2hRatesWeight + recentH2hDrawRate * recentH2hRatesWeight;
    }
}
