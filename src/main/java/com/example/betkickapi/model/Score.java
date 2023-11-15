package com.example.betkickapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Score {
    private Integer home;
    private Integer away;
    private Integer penaltiesHome;
    private Integer penaltiesAway;
}
