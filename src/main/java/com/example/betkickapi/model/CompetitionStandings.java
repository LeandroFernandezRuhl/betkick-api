package com.example.betkickapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CompetitionStandings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "competition", referencedColumnName = "id")
    private Competition competition;
    @Column(name = "group_field") // group is a reserved keyword
    @JsonProperty("group")
    private String group;
    @OneToMany(mappedBy = "competition")
    @JsonProperty("table")
    private List<Standing> standings;
}
