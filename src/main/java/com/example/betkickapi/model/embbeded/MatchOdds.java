package com.example.betkickapi.model.embbeded;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MatchOdds {
    private Double awayWinsOdds;
    private Double homeWinsOdds;
    private Double drawOdds;
    private Boolean temporaryRandomOdds; // Indicates whether the current odds are temporarily generated random values awaiting calculation

    public MatchOdds generateRandomOdds() {
        Random random = new Random();
        this.awayWinsOdds = BigDecimal.valueOf(1.1 + (4D - 0.1) * random.nextDouble())
                .setScale(1, RoundingMode.HALF_DOWN)
                .doubleValue();
        this.homeWinsOdds = BigDecimal.valueOf(1.1 + (4D - 0.1) * random.nextDouble())
                .setScale(1, RoundingMode.HALF_DOWN)
                .doubleValue();
        this.drawOdds = BigDecimal.valueOf(1.1 + (4D - 0.1) * random.nextDouble())
                .setScale(1, RoundingMode.HALF_DOWN)
                .doubleValue();
        this.temporaryRandomOdds = true;
        return this;
    }
}
