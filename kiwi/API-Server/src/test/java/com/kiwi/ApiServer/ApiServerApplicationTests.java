package com.kiwi.ApiServer;

import com.kiwi.ApiServer.DAO.SQLDAO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@SpringBootTest
class ApiServerApplicationTests {

	@Test
	public void selectTest() throws Exception {
		SQLDAO sql = new SQLDAO();
		ResultSet result = sql.selectTest();
		while(result.next()){
			System.out.println(result.getString("ID"));
		}
	}

}
