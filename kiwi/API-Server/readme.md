##프로그램 요구사항

mysql<br>
mvn

##사전 작업 (필수)
## 수정 파일
API-Server/src/main/resources/application.properties<br>
##변경사항
### mysql 주소 설정
`spring.datasource.url=jdbc:mysql://[mySqlDatabseUrl]:[mySqlDatabasePort]/[DB_NAME]?sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'&jdbcCompliantTruncation=false` <br>
[mySqlDatabseUrl]  : mysql database url (local : localhost) <br>
[mySqlDatabasePort] : mysql database port (default 3306) <br>
[DB\_NAME] : DB name (없으면 create database [database name]) <br>

### mysql 유저 / 비밀번호 설정
`spring.datasource.username=[DB_USERNAME]` <br>
`spring.datasource.password=[DB_PASSWORD]` <br>
[DB\_USERNAME] : DB Username (default : root) <br>
[DB\_PASSWORD] : DB Password

### mysql table 자동생성 설정
실행시 테이블 생성 -> 종료시 테이블 삭제 <br>
`spring.jpa.hibernate.ddl-auto=create-drop` <br>

실행시 테이블 생성 -> 종료시 테이블 유지 <br>
`spring.jpa.hibernate.ddl-auto=create`


##실행
`mvn spring-boot:run`

##MySQL 테이블 생성
```mysql
CREATE TABLE interview_participant(
    interview_id int,
    player_id int
);
```

