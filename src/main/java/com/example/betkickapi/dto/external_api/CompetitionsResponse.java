package com.example.betkickapi.dto.external_api;

import com.example.betkickapi.model.Competition;
import lombok.Data;

import java.util.List;


/**
 * DTO representing a response containing a list of competitions, received from  <a href="https://www.football-data.org/">football-data.org API</a>
 */
@Data
public class CompetitionsResponse {

    private List<Competition> competitions;
}