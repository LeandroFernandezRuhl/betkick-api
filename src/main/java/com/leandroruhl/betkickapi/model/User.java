package com.leandroruhl.betkickapi.model;

import com.leandroruhl.betkickapi.dto.internal_api.UserBetSummary;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * The User class represents a user entity in the system, extending {@link AbstractPersistableEntity} with a String identifier.
 * It includes information such as the user's name, email, account balance, and a list of associated bets.
 * This class also defines a named native query for finding user earnings and bet statistics, including a custom SQL query and result set mapping.
 * The result set mapping is used to construct instances of {@link UserBetSummary} from the query results.
 */
@NamedNativeQuery(
        name = "User.findEarningsAndBets",
        query =
                // Ordering logic: prioritize users based on a weighted combination of
                // win rate, total number of bets won, and net profit
                "SELECT ROW_NUMBER() OVER (ORDER BY " +
                        "(SUM(IF(B.is_won = TRUE, 1, 0)) * 100.0 / COUNT(B.id)) * 0.55 + " +
                        "(COUNT(B.id) * 0.3) + " +
                        "(SUM(IF(B.is_won = TRUE, B.amount * B.odds, 0)) - " +
                        "SUM(IF(B.is_won = FALSE, B.amount, 0))) * 0.15 DESC) AS position, " +

                        "U.login, " +
                        "SUM(IF(B.is_won = TRUE, B.amount * B.odds, 0)) - " +
                        "SUM(IF(B.is_won = FALSE, B.amount, 0)) AS earnings, " +
                        "SUM(IF(B.is_won = TRUE, 1, 0)) AS bets_won, " +
                        "SUM(IF(B.is_won = FALSE, 1, 0)) AS bets_lost " +
                        "FROM user U " +
                        "JOIN bet B ON U.id = B.user_id " +
                        "GROUP BY U.id, U.login " +
                        "HAVING SUM(IF(B.is_won = TRUE, 1, 0)) <> 0 OR " +
                        "SUM(IF(B.is_won = FALSE, 1, 0)) <> 0 " +
                        "ORDER BY position",
        resultSetMapping = "Mapping.UserBetSummary"
)
@SqlResultSetMapping(
        name = "Mapping.UserBetSummary",
        classes = @ConstructorResult(
                targetClass = UserBetSummary.class,
                columns = {
                        @ColumnResult(name = "position", type = Integer.class),
                        @ColumnResult(name = "login", type = String.class),
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
public class User extends AbstractPersistableEntity<UUID> {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;

    private String lastName;

    private String login;

    private String password;

    private Double accountBalance;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Bet> bets;

    public User(String firstName, String lastName, String login, String password, Double accountBalance, List<Bet> bets) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.password = password;
        this.accountBalance = accountBalance;
        this.bets = bets;
    }
}
