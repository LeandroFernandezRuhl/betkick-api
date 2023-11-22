package com.example.betkickapi.service.user;

import com.example.betkickapi.model.User;

public interface UserService {
    Boolean existsById(String id);

    void saveUser(User user);
}
