package com.example.betkickapi.service.user;

import com.example.betkickapi.model.User;

public interface UserService {
    Boolean existsById(String id);


    User decrementUserBalance(User user, Double amount);

    User incrementUserBalance(User user, Double amount);

    User findById(String id);

    void saveUser(User user);
}
