package com.leandroruhl.betkickapi.model.embbeded;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * The MatchOdds class is an embeddable component representing the odds for a football match.
 * It includes the odds for away wins, home wins, and draws. Additionally, it has a flag indicating whether
 * the current odds are temporarily generated random values awaiting calculation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MatchOdds {
    /**
     * The odds for away wins.
     */
    private Double awayWinsOdds;

    /**
     * The odds for home wins.
     */
    private Double homeWinsOdds;

    /**
     * The odds for a draw.
     */
    private Double drawOdds;

    /**
     * Indicates whether the current odds are temporarily generated random values awaiting calculation.
     */
    private Boolean temporaryRandomOdds;

    /**
     * Generates random odds for the match. This method sets the odds for away wins, home wins, and draws
     * to random values within a specified range and marks them as temporarily random odds.
     *
     * @return The {@code MatchOdds} instance with randomly generated odds.
     */
    public MatchOdds generateRandomOdds() {
        Random random = new Random();
        this.awayWinsOdds = BigDecimal.valueOf(1.1 + (2.5D - 0.1) * random.nextDouble())
                .setScale(1, RoundingMode.HALF_DOWN)
                .doubleValue();
        this.homeWinsOdds = BigDecimal.valueOf(1.1 + (2.5D - 0.1) * random.nextDouble())
                .setScale(1, RoundingMode.HALF_DOWN)
                .doubleValue();
        this.drawOdds = BigDecimal.valueOf(1.1 + (2.5D - 0.1) * random.nextDouble())
                .setScale(1, RoundingMode.HALF_DOWN)
                .doubleValue();
        this.temporaryRandomOdds = true;
        return this;
    }
}