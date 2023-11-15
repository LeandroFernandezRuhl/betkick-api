package com.example.betkickapi;

import com.example.betkickapi.service.FootballApiService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StartupDataInitializer implements ApplicationRunner {
    private FootballApiService footballApiService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        footballApiService.fetchAndSaveCompetitions();
        footballApiService.fetchAndSaveTodayMatches("2023-11-17", "2023-11-26");
    //      Thread.sleep(5000);
      //  footballApiService.fetchAndSaveTodayMatches("2023-11-17", "2023-11-27");
    }
}
