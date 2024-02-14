package com.example.betkickapi.dto.internal_api;

import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.enums.Winner;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO representing the response for a user's bet history.
 */
@Data
public class BetHistoryResponse {
    private Long id;
    private LocalDateTime placedAt;
    private Double odds;
    private Double amount;
    private Boolean isWon;
    private Winner winner;
    private Match match;
}

