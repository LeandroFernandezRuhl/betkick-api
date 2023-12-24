package com.example.betkickapi.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeadToHeadResponse {
    private List<Map<String, Object>> matches;
    private Double numberOfMatches;
    private TeamStatsResponse homeTeamStats;
    private TeamStatsResponse awayTeamStats;

    @JsonProperty("aggregates")
    private void unpackNested(Map<String, Object> aggregates) {
        this.numberOfMatches = ((Integer) aggregates.get("numberOfMatches")).doubleValue();
            Map<String, Object> home = (Map<String, Object>) aggregates.get("homeTeam");
            Map<String, Object> away = (Map<String, Object>) aggregates.get("awayTeam");

            TeamStatsResponse homeTeamStats = new TeamStatsResponse();
            TeamStatsResponse awayTeamStats = new TeamStatsResponse();

            homeTeamStats.setWins(((Integer) home.get("wins")).doubleValue());
            homeTeamStats.setDraws(((Integer) home.get("draws")).doubleValue());
            homeTeamStats.setLosses(((Integer) home.get("losses")).doubleValue());

            awayTeamStats.setWins(((Integer) away.get("wins")).doubleValue());
            awayTeamStats.setDraws(((Integer) away.get("draws")).doubleValue());
            awayTeamStats.setLosses(((Integer) away.get("losses")).doubleValue());

            this.homeTeamStats = homeTeamStats;
            this.awayTeamStats = awayTeamStats;
    }
}
