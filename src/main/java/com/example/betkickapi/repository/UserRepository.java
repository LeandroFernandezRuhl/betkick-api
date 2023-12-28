package com.example.betkickapi.repository;

import com.example.betkickapi.model.User;
import com.example.betkickapi.web.internal.UserBetSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Query(nativeQuery = true)
    List<UserBetSummary> findEarningsAndBets();
}
