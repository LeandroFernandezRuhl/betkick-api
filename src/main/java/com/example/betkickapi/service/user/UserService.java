package com.example.betkickapi.service.user;

import com.example.betkickapi.model.User;
import com.example.betkickapi.web.internal.UserBetSummary;

import java.util.List;

public interface UserService {
    Boolean existsById(String id);


    List<UserBetSummary> getUserLeaderboard();

    User decrementUserBalance(User user, Double amount);

    User incrementUserBalance(User user, Double amount);

    User findById(String id);

    User saveUser(User user);
}
