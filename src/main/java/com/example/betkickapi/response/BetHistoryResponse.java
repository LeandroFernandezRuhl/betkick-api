package com.example.betkickapi.response;

import com.example.betkickapi.model.Match;
import com.example.betkickapi.model.enums.Winner;
import lombok.Data;

import java.time.LocalDateTime;

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
