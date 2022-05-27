package com.kiwi.ApiServer.Service;

import com.kiwi.ApiServer.DAO.UserRepository;
import com.kiwi.ApiServer.DTO.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public interface MainPageService {

    public Optional<User> findByIdEmail(String id);
}
