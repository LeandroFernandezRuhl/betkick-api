package com.example.betkickapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The Standing class represents the standing of a team in a competition,
 * providing information such as position, team details, match statistics, and points.
 * Each standing object represent a row in the standings table of a specific competition ({@link CompetitionStandings}).
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Standing {

    /**
     * The unique identifier for the standing.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    /**
     * The position of the team in the standings.
     */
    private Integer position;

    /**
     * The team associated with this standing.
     */
    @ManyToOne
    @JoinColumn(name = "teamId")
    private Team team;

    /**
     * The competition standings to which this standing belongs.
     */
    @ManyToOne
    @JoinColumn(name = "competition", referencedColumnName = "id")
    private CompetitionStandings competition;

    /**
     * The number of matches won by the team.
     */
    private Integer won;

    /**
     * The number of matches drawn by the team.
     */
    private Integer draw;

    /**
     * The number of matches lost by the team.
     */
    private Integer lost;

    /**
     * The total points earned by the team.
     */
    private Integer points;

    /**
     * The total goals scored by the team.
     */
    private Integer goalsFor;

    /**
     * The total goals conceded by the team.
     */
    private Integer goalsAgainst;

    /**
     * The goal difference (goals scored minus goals conceded) for the team.
     */
    private Integer goalDifference;
}
