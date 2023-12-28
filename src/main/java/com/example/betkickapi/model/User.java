package com.example.betkickapi.model;

import com.example.betkickapi.web.internal.UserBetSummary;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@NamedNativeQuery(
        name = "User.findEarningsAndBets",
        query =
                "SELECT U.name, " +
                        "SUM(CASE WHEN B.is_won = TRUE THEN B.amount * B.odds ELSE 0 END) - " +
                        "SUM(CASE WHEN B.is_won = FALSE THEN B.amount ELSE 0 END) AS earnings, " +
                        "SUM(CASE WHEN B.is_won = TRUE THEN 1 ELSE 0 END) AS bets_won, " +
                        "SUM(CASE WHEN B.is_won = FALSE THEN 1 ELSE 0 END) AS bets_lost " +
                        "FROM user U " +
                        "JOIN bet B ON U.id = B.user_id " +
                        "GROUP BY U.id, U.name " +
                        "ORDER BY SUM(CASE WHEN B.is_won = TRUE THEN 1 ELSE 0 END) - " +
                        "SUM(CASE WHEN B.is_won = FALSE THEN 1 ELSE 0 END) DESC, " +
                        "SUM(CASE WHEN B.is_won = TRUE THEN B.amount * B.odds ELSE 0 END) - " +
                        "SUM(CASE WHEN B.is_won = FALSE THEN B.amount ELSE 0 END) DESC",
        resultSetMapping = "Mapping.UserBetSummary"
)
@SqlResultSetMapping(
        name = "Mapping.UserBetSummary",
        classes = @ConstructorResult(
                targetClass = UserBetSummary.class,
                columns = {
                        @ColumnResult(name = "name", type = String.class),
                        @ColumnResult(name = "earnings", type = Double.class),
                        @ColumnResult(name = "bets_won", type = Long.class),
                        @ColumnResult(name = "bets_lost", type = Long.class)
                }
        )
)
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