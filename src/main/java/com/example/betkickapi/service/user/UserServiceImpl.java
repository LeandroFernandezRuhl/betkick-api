package com.example.betkickapi.service.user;


import com.example.betkickapi.model.User;
import com.example.betkickapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Override
    public Boolean existsById(String id) {
        return userRepository.existsById(id);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }
}
