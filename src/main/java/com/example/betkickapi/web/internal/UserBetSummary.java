package com.example.betkickapi.web.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBetSummary {
    private String name;
    private Double earnings;
    private Long betsWon;
    private Long betsLost;
}