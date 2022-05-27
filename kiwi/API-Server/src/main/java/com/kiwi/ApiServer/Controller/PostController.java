package com.kiwi.ApiServer.Controller;

import com.kiwi.ApiServer.DAO.InterviewRepository;
import com.kiwi.ApiServer.DAO.SQLDAO;
import com.kiwi.ApiServer.DAO.UserRepository;
import com.kiwi.ApiServer.DTO.*;
import com.kiwi.ApiServer.DTO.Evaluation.*;
import com.kiwi.ApiServer.DTO.Interview.CreateInterview;
import com.kiwi.ApiServer.Response.SingleResult;
import com.kiwi.ApiServer.Security.JwtTokenProvider;
import com.kiwi.ApiServer.Service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PostController {
    @Autowired
    FileStorageService storageService;
    @Autowired
    ApplicationContext context;

//  password encoding -> 아직 적용 안함
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;

    // 회원가입
    @PostMapping(value = "/signup")
    public SingleResult join(@RequestBody Map<String, String> user) {
        SingleResult result = new SingleResult();
        System.out.println(user.get("memberType"));
        if(user.get("memberType").equals("1")){
            Long id = userRepository.save(User.builder()
                    .email(user.get("email"))
//                .password(passwordEncoder.encode(user.get("password")))
                    .password(user.get("password"))
                    .name(user.get("name"))
                    .memberType(1)
                    .roles(Collections.singletonList("ROLE_INTERVIEWEE")) // 면접관
                    .build()).getId();
            result.setResult(200);
            result.setMessage("success");
            result.setData(id);
        }else if(user.get("memberType").equals("2")){
            Long id = userRepository.save(User.builder()
                    .email(user.get("email"))
//                .password(passwordEncoder.encode(user.get("password")))
                    .password(user.get("password"))
                    .name(user.get("name"))
                    .memberType(2)
                    .roles(Collections.singletonList("ROLE_INTERVIEWER")) // 자면접 대상
                    .build()).getId();
            result.setResult(200);
            result.setMessage("success");
            result.setData(id);
        }else{
            result.setResult(400);
            result.setMessage("error");
        }
        return result;
    }

    // 로그인
    @PostMapping("/signin")
    public SingleResult login(@RequestBody Map<String, String> user) {
//        Environment env = context.getEnvironment();
//        System.out.println(env.getProperty("spring.datasource.url"));

        SingleResult result = new SingleResult();

//        User member = userRepository.findByEmail(user.get("email"))
//                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 E-MAIL 입니다."));

        User member = userRepository.findByEmail(user.get("email"))
                .orElse(null);
        if(member == null){
//            System.out.println("null");
            result.setResult(400);
            result.setMessage("user not found");
        }else{
//            System.out.println(member.toString());

            if(!user.get("password").equals(member.getPassword())){
//                throw new IllegalArgumentException("잘못된 비밀번호입니다.");
                result.setResult(401);
                result.setMessage("wrong password");
            }else{
                result.setResult(200);
                result.setMessage("success");
                result.setData(jwtTokenProvider.createToken(member.getUsername(), member.getRoles()));
            }
        }
        return result;
    }

    @PostMapping(value = "createInterview")
    public SingleResult createInterview(HttpServletRequest request, @RequestBody CreateInterview createInterview) throws Exception{
        SingleResult singleResult = new SingleResult();
        SQLDAO sqldao = new SQLDAO();
        String interview_id = "";
        String token = request.getHeader("X-AUTH-TOKEN");

        ResultSet res = sqldao.createInterview(createInterview.getInterviewName(),createInterview.getStartDate(),createInterview.getStartTime(),createInterview.getTemplate());
        while(res.next()){
            interview_id = res.getString("id");
        }

        String user = jwtTokenProvider.getUser(token);
        sqldao.insertInterviewParticipant(interview_id,user);

        for(Iterator<String> iter = createInterview.getInterviewee().iterator(); iter.hasNext(); ){
            String intervieweeEmail = iter.next();
            sqldao.insertInterviewParticipant(interview_id,intervieweeEmail);
        }

        for(Iterator<String> iter = createInterview.getInterviewer().iterator(); iter.hasNext(); ){
            String interviewerEmail = iter.next();
            sqldao.insertInterviewParticipant(interview_id,interviewerEmail);
        }
        singleResult.setResult(200);
        singleResult.setMessage("SUCCESS");
        return singleResult;
    }

    @PostMapping(value = "insertResume")
    public ResponseEntity<ResponseMessage> insertResume(HttpServletRequest request, @RequestParam("file") MultipartFile file) throws Exception{
        SQLDAO sqldao = new SQLDAO();
        String token = request.getHeader("X-AUTH-TOKEN");
        String email = jwtTokenProvider.getUser(token);
        int user_id = sqldao.getUserIdFromEmail(email);

        sqldao.insertResume(user_id,file.getOriginalFilename());

        String message = "";
        try{
            storageService.save(file);

            message = "Uploaded the file sucessfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch(Exception e){
            message = "Could not upload the file: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    @PostMapping(value = "createEvaluation")
    public SingleResult createEvaluation (HttpServletRequest request, @RequestBody Evaluation evaluation) throws Exception{
        SingleResult result = new SingleResult();
        SQLDAO sqldao = new SQLDAO();

        String token = request.getHeader("X-AUTH-TOKEN");
        String email = jwtTokenProvider.getUser(token);
        int user_id = sqldao.getUserIdFromEmail(email);

        int evaluationId = sqldao.insertEvaluation(evaluation.getName(),user_id);
        for(EvaluationList evaluationList: evaluation.getEvaluationList()){
            String category = evaluationList.getCategory();
            String title = evaluationList.getTitle();
            int type = evaluationList.getType();
            System.out.println(evaluationList.getData());
            int data = 0;
            try{
                data = Integer.parseInt(evaluationList.getData());
            } catch (Exception exception) {
                System.out.println(exception);
            }
            sqldao.insertEvaluationQuestion(evaluationId,type,title,category,data);
        }
//        int evaluationId = sqldao.insertEvaluation(evaluation.getName());
//        for(EvaluationList evaluationList : evaluation.getEvaluationList()){
//            String category = evaluationList.getCategory();
//            for(EvaluationQuestion evaluationQuestion: evaluationList.getQuestions()){
//                int type = evaluationQuestion.getType();
//                String title = evaluationQuestion.getTitle();
//                int question_id = sqldao.insertEvaluationQuestion(evaluationId,type,title,category);
//                System.out.println(question_id);
////                if(type == 1){
////                    for(String choice : evaluationQuestion.getData()){
////                        sqldao.insertEvaluationChoice(question_id,choice);
////                    }
////                }
//            }
//        }

        result.setResult(200);
        result.setMessage("SUCCESS");

        return result;
    }

    @PostMapping(value = "createEvaluationResult")
    public SingleResult createEvaluationResult (HttpServletRequest request, @RequestBody EvaluationResultList evaluationResultList) throws Exception{
        SingleResult singleResult = new SingleResult();

        SQLDAO sqldao = new SQLDAO();
        String token = request.getHeader("X-AUTH-TOKEN");
        String email = jwtTokenProvider.getUser(token);

//        면접관 이름
        ResultSet tmp = sqldao.getUsernameFromEmail(email);
        String interviewer = "";
        while(tmp.next())
            interviewer = tmp.getString(1);

        for(EvaluationResult evaluationResult : evaluationResultList.getEvaluationResultList()){
//          면접자 이름
            String interviewee = evaluationResult.getLabel();

            Evaluation evaluation = evaluationResult.getEvaluation();
            List<EvaluationList> evaluationListList = evaluation.getEvaluationList();

            String evaluationName = evaluation.getName();

            for(EvaluationList evlList : evaluationListList){
                List<EvaluationQuestion> evaluationQuestionList = evlList.getQuestions();

                String category = evlList.getCategory();

                for(EvaluationQuestion evaluationQuestion : evaluationQuestionList){
                    String questionName = evaluationQuestion.getTitle();
                    int type = evaluationQuestion.getType();
                    String data = evaluationQuestion.getData();

                    String path = "./results/";
                    String fileName = evaluationName + ".csv";
                    File csv = new File(path + fileName);
                    BufferedWriter bw = null;
                    try{
                        bw = new BufferedWriter(new FileWriter(csv, true));
                        String line = interviewee + "," + evaluationName + "," + category + "," + Integer.toString(type) + "," +  questionName + "," +  data;
                        bw.write(line);
                        bw.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (bw != null) {
                                bw.flush(); // 남아있는 데이터까지 보내 준다
                                bw.close(); // 사용한 BufferedWriter를 닫아 준다
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return singleResult;
    }

//    @PostMapping(value = "upload")
//    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file){
//        String message = "";
//        try{
//            storageService.save(file);
//
//            message = "Uploaded the file sucessfully: " + file.getOriginalFilename();
//            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
//        } catch(Exception e){
//            message = "Could not upload the file: " + file.getOriginalFilename();
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
//        }
//    }
}
