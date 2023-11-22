package com.example.betkickapi.model;

import com.example.betkickapi.model.enums.Duration;
import com.example.betkickapi.model.enums.Status;
import com.example.betkickapi.model.enums.Winner;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "football_match") // match is a reserved keyword in mysql
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Match extends AbstractPersistableEntity<Integer> {
    @Id
    @EqualsAndHashCode.Include
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "competitionId", referencedColumnName = "id")
    private Competition competition;
    @Column(name = "date") // utcDate is a reserved keyword in mysql
    private LocalDateTime utcDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Winner winner;
    @Enumerated(EnumType.STRING)
    private Duration duration;
    @Embedded
    private Score score;
    @ManyToOne
    @JoinColumn(name = "homeTeamId")
    private Team homeTeam;
    @ManyToOne
    @JoinColumn(name = "awayTeamId")
    private Team awayTeam;


    @JsonProperty("score")
    private void unpackNested(Map<String, Object> score) {
        this.score = new Score();
        Map<String, Integer> fullTimeScore = (Map<String, Integer>) score.get("fullTime");
        String winner = (String) score.get("winner");
        this.winner = winner == null ? null : Winner.valueOf(winner);
        this.duration = Duration.valueOf((String) score.get("duration"));
        if (this.duration == Duration.PENALTY_SHOOTOUT) {
            Map<String, Integer> penalties = (Map<String, Integer>) score.get("penalties");
            Integer penaltiesHome = (penalties.get("home"));
            Integer penaltiesAway = (penalties.get("away"));
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
