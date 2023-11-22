package com.example.betkickapi.model;

import com.example.betkickapi.model.enums.Winner;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Bet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private Date placedAt;
    private Double odds;
    private Double amount;
    private Boolean isWon;
    @Enumerated(EnumType.STRING)
    private Winner winner;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match; //validate that matches ids in a given list are different
    //@Embedded
    //private Score score;
}
