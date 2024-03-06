package com.leandroruhl.betkickapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The Competition class represents a football competition.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Competition extends AbstractPersistableEntity<Integer> {

    /**
     * The unique identifier for the competition.
     */
    @Id
    @EqualsAndHashCode.Include
    private Integer id;

    /**
     * The name of the competition.
     */
    private String name;

    /**
     * The short name or abbreviation of the competition.
     */
    private String shortName;

    /**
     * The code associated with the competition.
     */
    private String code;

    /**
     * The emblem or image URL representing the competition.
     */
    private String emblem;
}
