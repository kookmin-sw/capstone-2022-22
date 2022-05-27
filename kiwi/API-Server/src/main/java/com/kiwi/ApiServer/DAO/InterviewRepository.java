package com.kiwi.ApiServer.DAO;

import com.kiwi.ApiServer.Table.interview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<interview, Long> {
}
