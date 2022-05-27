package com.kiwi.ApiServer.DAO;

import com.kiwi.ApiServer.DTO.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
