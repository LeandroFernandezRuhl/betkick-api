package com.example.betkickapi.dto.external_api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO representing a response containing head-to-head statistics between two teams, received from
 * <a href="https://www.football-data.org/">football-data.org API</a>.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeadToHeadResponse {

    /**
     * The list of matches between the two teams.
     */
    private List<Map<String, Object>> matches;

    /**
     * The total number of matches played between the two teams.
     */
    private Double numberOfMatches;

    /**
     * Statistics for the home team in the head-to-head matches.
     */
    private TeamStatsResponse homeTeamStats;

    /**
     * Statistics for the away team in the head-to-head matches.
     */
    private TeamStatsResponse awayTeamStats;

    /**
     * Unpacks and maps nested aggregates from the API response.
     *
     * @param aggregates The nested aggregates map containing additional information.
     */
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
