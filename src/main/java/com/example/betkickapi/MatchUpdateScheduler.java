package com.example.betkickapi;

import com.example.betkickapi.service.CacheService;
import com.example.betkickapi.service.FootballApiService;
import com.example.betkickapi.service.match.MatchService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
@Slf4j
public class MatchUpdateScheduler {
    private FootballApiService footballApiService;
    private MatchService matchService;
    private Boolean matchesToday;

    public MatchUpdateScheduler(FootballApiService footballApiService, MatchService matchService) {
        this.footballApiService = footballApiService;
        this.matchService = matchService;
        this.matchesToday = false;
    }

    // a game's score, status and duration can change any second
    // but polling the Football Data Org API every second is not feasible
    // so the games of the current day are checked every minute
    @Scheduled(cron = "0 * * * * ?") // Cron expression for every minute
    @Transactional
    public void updateMatches() {
        log.info("Scheduled task to update matches is being executed");
        if (matchesToday) {
            footballApiService.fetchAndUpdateMatches();
        }
    }

   // @Scheduled(cron = "0 */2 * * * *")
    /*@Transactional
    public void updateMatchesTest() {
        log.info("Scheduled task to update matches is being executed");
        if (matchesToday) {
            footballApiService.fetchAndUpdateMatchesTest();
        }
    } */

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkMatchesToday() {
        this.matchesToday = matchService.areThereMatchesToday();
        log.info(this.matchesToday.toString());
    }

    // a game's status and programmed date can change anytime,
    // so it needs to be checked daily
    @Scheduled(cron = "0 0 0 * * ?") // Cron expression for midnight (00:00:00) every day
    @Transactional
    public void saveUpcomingMatches() {
        Instant currentInstant = Instant.now();
        LocalDate currentDate = LocalDate.ofInstant(currentInstant, ZoneOffset.UTC);
        footballApiService.fetchAndSaveMatches(
                currentDate,
                currentDate.plusDays(10),
                true);
        footballApiService.fetchAndSaveMatches(
                currentDate.plusDays(10),
                currentDate.plusDays(20),
                true);
        footballApiService.fetchAndSaveMatches(
                currentDate.plusDays(20),
                currentDate.plusDays(30),
                true);
    }
}
