package com.example.betkickapi.response;

import com.example.betkickapi.model.Competition;
import lombok.*;

import java.util.List;


@Data
public class CompetitionsResponse {
    private List<Competition> competitions;
}
