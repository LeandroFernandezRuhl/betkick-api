package com.example.betkickapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class User extends AbstractPersistableEntity<String> {
    @Id
    @EqualsAndHashCode.Include
    private String id; // auth0 user id
    private String name;
    private String email;
    private Double accountBalance;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER) // o join fetch
    private List<Bet> bets;

}