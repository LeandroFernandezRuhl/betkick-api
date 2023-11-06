package com.example.betkickapi.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Team {
    private Integer id;
    private String name;
    private String shortName;
    private String tla; // tla = three letter acronym
    private String crest; // url
}
