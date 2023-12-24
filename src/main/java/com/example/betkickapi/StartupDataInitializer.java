package com.example.betkickapi;

import com.example.betkickapi.service.utility.FootballApiService;
import com.example.betkickapi.service.competition.CompetitionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
@AllArgsConstructor
@Slf4j
public class StartupDataInitializer implements ApplicationRunner {
    private FootballApiService footballApiService;
    private MatchUpdateScheduler matchUpdateScheduler;
    private CompetitionService competitionService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // wait a bit before initializing
        Thread.sleep(10000);
        System.out.println("INITIALIZATION FINISHED");
        competitionService.saveCompetitions(footballApiService.fetchCompetitions());
        // get this month's matches, has to be done in 10 days intervals because of API restriction
        Instant currentInstant = Instant.now();
        LocalDate currentDate = LocalDate.ofInstant(currentInstant, ZoneOffset.UTC);
        footballApiService.fetchAndSaveMatches(
                currentDate,
                currentDate.plusDays(10),
                false);
        footballApiService.fetchAndSaveMatches(
                currentDate.plusDays(10),
                currentDate.plusDays(20),
                false);
        footballApiService.fetchAndSaveMatches(
                currentDate.plusDays(20),
                currentDate.plusDays(30),
                false);
        // wait for API requests to refill
        Thread.sleep(60000);
        System.out.println("API REQUESTS REPLENISHED");
        matchUpdateScheduler.checkMatchesToday();
        matchUpdateScheduler.setShouldCalculateMatchOdds(true);
    }
}
