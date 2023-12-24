package com.example.betkickapi.model.embbeded;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Score {
    private Integer home;
    private Integer away;
    private Integer penaltiesHome;
    private Integer penaltiesAway;
}
