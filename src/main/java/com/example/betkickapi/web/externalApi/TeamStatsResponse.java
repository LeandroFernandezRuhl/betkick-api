package com.example.betkickapi.web.externalApi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamStatsResponse {
    private List<Map<String, Object>> matches;
    // stats over the last 2 years
    private Double wins;
    private Double draws;
    private Double losses;
    // stats over the last 5 games (or less if the team is new and doesn't have 5 games)
    private Double recentWins;
    private Double recentDraws;
    private Double recentLosses;

    @JsonProperty("resultSet")
    private void unpackNestedResultSet(Map<String, Object> resultSet) {
        this.wins = resultSet.get("wins") == null ? 0 : ((Integer) resultSet.get("wins")).doubleValue();
        this.draws = resultSet.get("draws") == null ? 0 : ((Integer) resultSet.get("draws")).doubleValue();
        this.losses = resultSet.get("losses") == null ? 0 : ((Integer) resultSet.get("losses")).doubleValue();
    }

    // this method needs to be called manually because teamName argument can't be passed when deserializing automatically
    public void unpackNestedMatches(Integer teamId) {
        int count = 0;
        recentWins = 0d;
        recentDraws = 0d;
        recentLosses = 0d;
        if (matches.isEmpty())
            return;
        for (int i = matches.size() - 1; i >= 0 && count < 7; i--) {
            Map<String, Object> match = matches.get(i);
            if (match.isEmpty())
                break;

            String winner = (String) ((Map<String, Object>) match.get("score")).get("winner");
            if (winner == null)
                continue;

            Integer homeTeamId = (Integer) ((Map<String, Object>) match.get("homeTeam")).get("id");
            Integer awayTeamId = (Integer) ((Map<String, Object>) match.get("awayTeam")).get("id");

            boolean isTeamHome = homeTeamId.equals(teamId);
            boolean isTeamAway = awayTeamId.equals(teamId);

            if ((isTeamHome && winner.equals("HOME_TEAM")) || (isTeamAway && winner.equals("AWAY_TEAM"))) {
                recentWins++;
            } else if (winner.equals("DRAW")) {
                recentDraws++;
            } else {
                recentLosses++;
            }
            count++;
        }
    }
}
