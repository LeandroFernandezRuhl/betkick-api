package com.example.betkickapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Competition extends AbstractPersistableEntity<Integer> {
    @Id
    @EqualsAndHashCode.Include
    private Integer id;
    private String name;
    private String shortName;
    private String code;
    private String emblem; // image url
}
