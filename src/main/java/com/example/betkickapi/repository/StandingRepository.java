package com.example.betkickapi.repository;

import com.example.betkickapi.model.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandingRepository extends JpaRepository<Standing, Integer> {
}
