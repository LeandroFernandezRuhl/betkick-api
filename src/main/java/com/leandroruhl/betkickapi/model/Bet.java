package com.leandroruhl.betkickapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.leandroruhl.betkickapi.model.enums.Winner;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The Bet class represents a betting activity on a football match.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Bet {

    /**
     * The unique identifier for the bet.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * The date and time when the bet was placed in UTC format.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime placedAt;

    /**
     * The odds associated with the bet.
     */
    private Double odds;

    /**
     * The amount of the bet.
     */
    private Double amount;

    /**
     * A flag indicating whether the bet is won or lost.
     */
    private Boolean isWon;

    /**
     * The winner of the bet, if applicable.
     */
    @Enumerated(EnumType.STRING)
    private Winner winner;

    /**
     * The user who placed the bet.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The football match associated with the bet.
     */
    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;
}
