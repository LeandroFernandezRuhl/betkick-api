package com.example.betkickapi;

import com.example.betkickapi.model.Competition;
import com.example.betkickapi.service.competition.CompetitionService;
import com.example.betkickapi.service.utility.FootballApiService;
import com.example.betkickapi.web.externalApi.StandingsResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class StartupDataInitializer implements ApplicationRunner {
    private FootballApiService footballApiService;
    private JobScheduler jobScheduler;
    private CompetitionService competitionService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // wait a bit before initializing
        Thread.sleep(10000);
        System.out.println("INITIALIZATION FINISHED");
        competitionService.saveCompetitions(footballApiService.fetchCompetitions()); // 1 request
        jobScheduler.saveUpcomingMatches(); // 9 requests
        Thread.sleep(60000); // 10 requests made within a minute so wait till refill
        List<Competition> competitions = competitionService.getCompetitions();
        List<StandingsResponse> responses = new ArrayList<>();
        for (int i = 0; i < 6; i++) { // 6 requests, one per competition
            responses.add(footballApiService.fetchStandings(competitions.get(i)));
        }
        Thread.sleep(60000); // wait for API requests to refill
        for (int i = 6; i < 12; i++) { // 6 requests, one per competition (total will always be 12)
            responses.add(footballApiService.fetchStandings(competitions.get(i)));
        }
        // wait is needed because the 12 standings need to be saved together
        footballApiService.saveStandings(responses);
        // wait for API requests to refill so scheduled methods can execute properly
        Thread.sleep(60000);
        jobScheduler.setSecondaryTasksCanExecute(true);
        jobScheduler.checkMatchesToday();
        jobScheduler.setShouldCalculateMatchOdds(true);
    }
}
