package com.leandroruhl.betkickapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The Team class represents a team entity in the system, extending {@link AbstractPersistableEntity} with an Integer identifier.
 * It includes information such as the team's name, short name, three-letter acronym (tla), and crest (image URL).
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Team extends AbstractPersistableEntity<Integer> {

    /**
     * The team's identifier.
     */
    @Id
    @EqualsAndHashCode.Include
    private Integer id;

    /**
     * The team's name.
     */
    private String name;

    /**
     * The team's short name.
     */
    private String shortName;

    /**
     * The team's three-letter acronym (tla).
     */
    private String tla;

    /**
     * The team's crest (image URL).
     */
    private String crest;
}
