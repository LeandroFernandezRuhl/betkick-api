package com.leandroruhl.betkickapi.dto.internal_api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * DTO representing the summary of a user's betting performance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBetSummary {
    private Integer position;
    private String login;
    private Double earnings;
    private Long betsWon;
    private Long betsLost;
}
