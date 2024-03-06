package com.leandroruhl.betkickapi.model.embbeded;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Score class is an embeddable component representing the scores in a football match.
 * It includes the scores for the home and away teams, as well as scores during penalty shootouts if applicable.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Score {

    /**
     * The score for the home team.
     */
    private Integer home;

    /**
     * The score for the away team.
     */
    private Integer away;

    /**
     * The number of penalties scored by the home team during a penalty shootout.
     */
    private Integer penaltiesHome;

    /**
     * The number of penalties scored by the away team during a penalty shootout.
     */
    private Integer penaltiesAway;
}
