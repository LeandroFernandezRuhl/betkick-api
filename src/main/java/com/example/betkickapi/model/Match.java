package com.example.betkickapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Match {
    private Competition competition;
    private Score score;
    private Integer id;
    private Date utcDate; // make string if date doesn't work
    private String status; // enum?
    private Team homeTeam;
    private Team awayTeam;


    @JsonProperty("score")
    private void unpackNested(Map<String, Object> score) {
        this.score = new Score();
        Map<String, Integer> fullTimeScore = (Map<String, Integer>) score.get("fullTime");
        this.score.setHome(fullTimeScore.get("home"));
        this.score.setAway(fullTimeScore.get("away"));
    }
}
