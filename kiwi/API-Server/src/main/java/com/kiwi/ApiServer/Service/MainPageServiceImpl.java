package com.kiwi.ApiServer.Service;

import com.kiwi.ApiServer.DAO.UserRepository;
import com.kiwi.ApiServer.DTO.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MainPageServiceImpl implements MainPageService{
    @Autowired
    UserRepository userRepository;

    @Override
    public Optional<User> findByIdEmail(String id) {
        return userRepository.findByEmail(id);
    }
}
