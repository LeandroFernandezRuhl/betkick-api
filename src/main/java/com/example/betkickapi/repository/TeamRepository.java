package com.example.betkickapi.repository;

import com.example.betkickapi.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
    @Query(value = "SELECT * FROM team", nativeQuery = true)
    Set<Team> findAllTeams();
}
