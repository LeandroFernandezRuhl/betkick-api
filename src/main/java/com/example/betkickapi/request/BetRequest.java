package com.example.betkickapi.request;

import com.example.betkickapi.model.enums.Winner;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BetRequest {
    @NotNull(message = "Bet odds must be provided")
    @Positive
    private Double betOdds;

    @NotNull(message = "Bet amount must be provided")
    @Positive(message = "Bet amount must be a positive")
    private Double betAmount;

    @NotNull(message = "Match ID must be provided")
    @Positive
    private Integer matchId;

    @NotNull(message = "Winner must be provided")
    private Winner winner;
}
