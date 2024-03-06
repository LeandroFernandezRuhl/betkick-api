package com.leandroruhl.betkickapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The standings of a competition are typically represented as a table, where each row is the standing of a team.
 * The CompetitionStandings class is that "table" that holds all the standings ({@link Standing}) of a particular competition.
 * If the competition has groups, then there will be multiple CompetitionStandings objects, each representing the standings of a group
 * in that particular competition.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CompetitionStandings {

    /**
     * The unique identifier for the competition standings.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    /**
     * The competition to which the standings belong.
     */
    @ManyToOne
    @JoinColumn(name = "competition", referencedColumnName = "id")
    private Competition competition;

    /**
     * The group to which the standings belong. Can be null if the competition does not have groups.
     */
    @Column(name = "group_field") // "group" is a keyword in MySQL/MariaDB
    @JsonProperty("group")
    private String group;

    /**
     * The list of standings for teams in the competition.
     */
    @OneToMany(mappedBy = "competition")
    @JsonProperty("table")
    private List<Standing> standings;
}

