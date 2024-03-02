package com.example.betkickapi;

import com.example.betkickapi.dto.external_api.StandingsResponse;
import com.example.betkickapi.model.*;
import com.example.betkickapi.model.embbeded.MatchOdds;
import com.example.betkickapi.model.embbeded.Score;
import com.example.betkickapi.model.enums.Duration;
import com.example.betkickapi.model.enums.Status;
import com.example.betkickapi.model.enums.Winner;
import com.example.betkickapi.service.bet.BetService;
import com.example.betkickapi.service.competition.CompetitionService;
import com.example.betkickapi.service.match.MatchService;
import com.example.betkickapi.service.team.TeamService;
import com.example.betkickapi.service.user.UserService;
import com.example.betkickapi.service.utility.FootballApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class StartupDataInitializer implements ApplicationRunner {
    private final FootballApiService footballApiService;
    private final JobScheduler jobScheduler;
    private final CompetitionService competitionService;
    private final UserService userService;
    private final BetService betService;
    private final TeamService teamService;
    private final MatchService matchService;
    @Value("${app.initialization.flag}")
    private boolean executeInitialization;

    public StartupDataInitializer(FootballApiService footballApiService, JobScheduler jobScheduler, CompetitionService competitionService,
                                  UserService userService, BetService betService, TeamService teamService, MatchService matchService) {
        this.footballApiService = footballApiService;
        this.jobScheduler = jobScheduler;
        this.competitionService = competitionService;
        this.userService = userService;
        this.betService = betService;
        this.teamService = teamService;
        this.matchService = matchService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (executeInitialization) {
            // Wait a bit before initializing
            Thread.sleep(10000);
            System.out.println("INITIALIZATION FINISHED");
            competitionService.saveCompetitions(footballApiService.fetchCompetitions()); // 1 request
            jobScheduler.saveUpcomingMatches(); // 9 requests
            Thread.sleep(60000); // 10 requests made within a minute so wait till refill
            List<Competition> competitions = competitionService.getAllCompetitions();
            List<StandingsResponse> responses = new ArrayList<>();
            for (int i = 0; i < 6; i++) { // 6 requests, one per competition
                responses.add(footballApiService.fetchStandings(competitions.get(i)));
            }
            Thread.sleep(60000); // wait for API requests to refill
            for (int i = 6; i < 12; i++) { // 6 requests, one per competition (total will always be 12)
                responses.add(footballApiService.fetchStandings(competitions.get(i)));
            }
            // Wait is needed because the 12 standings need to be saved together
            footballApiService.saveStandings(responses);
            // Wait for API requests to refill so scheduled methods can execute properly
            Thread.sleep(60000);
            jobScheduler.setSecondaryTasksCanExecute(true);
            jobScheduler.checkMatchesToday();
            jobScheduler.setShouldCalculateMatchOdds(true);


            // Pre-populate DB
            String[] usernames = {
                    "Juan_Smith89",
                    "MariaE*23",
                    "A_M123",
                    "Isabella.Clark$",
                    "Ricardo.T1987",
                    "Cami-D",
                    "MateoW!",
                    "Eva@Brown",
                    "DiegoJ_87",
                    "AnaW456",
                    "MiguelA!32",
                    "Sofia_Martin%",
                    "C_Moore89",
                    "Luis.Robinson42",
                    "Andrea_Cooper",
                    "Luisa.Perez$",
                    "Nicolas_Brown76",
                    "Amelia-Turner",
                    "Guillermo.Hall32",
                    "Valentina_Cruz#",
                    "R_Fisher",
                    "Mia.Harrison@",
                    "Jose_Reed65",
                    "Sophie.Gardner",
                    "Ruben.Evans&",
                    "Carmen.Hill_56",
                    "Tom.Cox12",
                    "Natalia.Perry09",
                    "Samuel.Barnes"
            };

            List<User> users = new ArrayList<>(30);
            for (String username : usernames) {
                User user = new User("First", "Last", username, UUID.randomUUID().toString(),
                        1000D, new ArrayList<>());
                User savedUser = userService.saveUser(user);
                users.add(savedUser);
            }

            Team fakeTeam1 = new Team(999999, "Fake Team 1", "Fake 1", "FK1", "");
            Team fakeTeam2 = new Team(999998, "Fake Team 2", "Fake 2", "FK2", "");
            teamService.saveTeam(fakeTeam1);
            teamService.saveTeam(fakeTeam2);

            Match fakeMatch = new Match(999999999, competitions.get(0), LocalDateTime.now(), Status.FINISHED,
                    Winner.AWAY_TEAM, Duration.REGULAR, new Score(1, 1, null, null),
                    new MatchOdds(1.71, 1.58, 2.21, false),
                    fakeTeam1, fakeTeam2);
            matchService.saveMatch(fakeMatch);

            List<Bet> betList = new LinkedList<>();
            int falseCounter = 0;
            int trueCounter = 0;
            for (User user : users) {
                Random random = new Random();
                int betLimit = random.nextInt(100);
                // randomize the number of winning and losing users
                boolean isThisGuyWinning = random.nextInt(2) + 1 == 2;
                if (isThisGuyWinning)
                    trueCounter++;
                else
                    falseCounter++;
                for (int i = 0; i < betLimit + 1; i++) {
                    Bet bet = new Bet();
                    bet.setUser(user);
                    bet.setPlacedAt(LocalDateTime.now());
                    bet.setMatch(fakeMatch);
                    bet.setAmount(random.nextInt(1500) + 20D);
                    if (isThisGuyWinning)
                        switch (random.nextInt(4) + 1) {
                            case 1:
                            case 2: // add several cases to the winning result to have more winning bets
                                bet.setWinner(Winner.AWAY_TEAM);
                                bet.setOdds(fakeMatch.getOdds().getAwayWinsOdds());
                                bet.setIsWon(fakeMatch.getWinner() == Winner.AWAY_TEAM);
                                break;
                            case 3:
                                bet.setWinner(Winner.HOME_TEAM);
                                bet.setOdds(fakeMatch.getOdds().getHomeWinsOdds());
                                bet.setIsWon(fakeMatch.getWinner() == Winner.HOME_TEAM);
                                break;
                            case 4:
                                bet.setWinner(Winner.DRAW);
                                bet.setOdds(fakeMatch.getOdds().getDrawOdds());
                                bet.setIsWon(fakeMatch.getWinner() == Winner.DRAW);
                                break;
                        }
                    else
                        switch (random.nextInt(4) + 1) {
                            case 1:
                                bet.setWinner(Winner.AWAY_TEAM);
                                bet.setOdds(fakeMatch.getOdds().getAwayWinsOdds());
                                bet.setIsWon(fakeMatch.getWinner() == Winner.AWAY_TEAM);
                                break;
                            case 2: // add several cases to the losing result to have more losing bets
                            case 3:
                                bet.setWinner(Winner.HOME_TEAM);
                                bet.setOdds(fakeMatch.getOdds().getHomeWinsOdds());
                                bet.setIsWon(fakeMatch.getWinner() == Winner.HOME_TEAM);
                                break;
                            case 4:
                                bet.setWinner(Winner.DRAW);
                                bet.setOdds(fakeMatch.getOdds().getDrawOdds());
                                bet.setIsWon(fakeMatch.getWinner() == Winner.DRAW);
                                break;
                        }
                    betList.add(bet);
                }
            }
            betService.createFinishedBets(betList);
            log.info("False: " + falseCounter);
            log.info("True: " + trueCounter);
        } else {
            log.info("Initialization flag is set to false, skipping initialization");
            jobScheduler.setSecondaryTasksCanExecute(true);
            jobScheduler.checkMatchesToday();
            jobScheduler.setShouldCalculateMatchOdds(true);
        }
    }
}
