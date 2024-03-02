package com.example.betkickapi.model;

import com.example.betkickapi.model.embbeded.MatchOdds;
import com.example.betkickapi.model.embbeded.Score;
import com.example.betkickapi.model.enums.Duration;
import com.example.betkickapi.model.enums.Status;
import com.example.betkickapi.model.enums.Winner;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The Match class represents a football match, including details such as teams, scores, odds, and status.
 */
@Entity
@Table(name = "football_match") // "match" is a keyword in MySQL/MariaDB
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Match extends AbstractPersistableEntity<Integer> {

    /**
     * The unique identifier for the match.
     */
    @Id
    @EqualsAndHashCode.Include
    private Integer id;

    /**
     * The competition to which the match belongs.
     */
    @ManyToOne
    @JoinColumn(name = "competitionId", referencedColumnName = "id")
    private Competition competition;

    /**
     * The UTC date and time of the match.
     */
    @Column(name = "date") // utcDate is a reserved keyword in mysql
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime utcDate;

    /**
     * The status of the match (e.g., SCHEDULED, IN_PLAY, FINISHED).
     */
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * The winner of the match.
     */
    @Enumerated(EnumType.STRING)
    private Winner winner;

    /**
     * The duration of the match (e.g., REGULAR, EXTRA_TIME, PENALTY_SHOOTOUT).
     */
    @Enumerated(EnumType.STRING)
    private Duration duration;

    /**
     * The score of the match, including goals and penalties.
     */
    @Embedded
    private Score score;

    /**
     * The odds associated with the match.
     */
    @Embedded
    private MatchOdds odds;

    /**
     * The home team participating in the match.
     */
    @ManyToOne
    @JoinColumn(name = "homeTeamId")
    private Team homeTeam;

    /**
     * The away team participating in the match.
     */
    @ManyToOne
    @JoinColumn(name = "awayTeamId")
    private Team awayTeam;

    /**
     * Unpacks nested JSON data to set the score details.
     *
     * @param score A map containing score details.
     */
    @JsonProperty("score")
    private void unpackNested(Map<String, Object> score) {
        this.score = new Score();
        Map<String, Integer> fullTimeScore = (Map<String, Integer>) score.get("fullTime");
        String winner = (String) score.get("winner");
        this.winner = winner == null ? null : Winner.valueOf(winner);
        this.duration = Duration.valueOf((String) score.get("duration"));
        if (this.duration == Duration.PENALTY_SHOOTOUT) {
            Map<String, Integer> penalties = (Map<String, Integer>) score.get("penalties");
            Integer penaltiesHome = penalties.get("home");
            Integer penaltiesAway = penalties.get("away");
            this.score.setPenaltiesHome(penaltiesHome);
            this.score.setPenaltiesAway(penaltiesAway);
            this.score.setHome(fullTimeScore.get("home") - penaltiesHome);
            this.score.setAway(fullTimeScore.get("away") - penaltiesAway);
        } else {
            this.score.setHome(fullTimeScore.get("home"));
            this.score.setAway(fullTimeScore.get("away"));
        }
    }
}

