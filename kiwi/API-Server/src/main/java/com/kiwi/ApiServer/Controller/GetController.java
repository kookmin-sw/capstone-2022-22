package com.kiwi.ApiServer.Controller;

import com.kiwi.ApiServer.DAO.SQLDAO;
import com.kiwi.ApiServer.DTO.Evaluation.*;
import com.kiwi.ApiServer.DTO.Interview.InterviewParticipant;
import com.kiwi.ApiServer.DTO.User;
import com.kiwi.ApiServer.Response.SingleResult;
import com.kiwi.ApiServer.Security.JwtTokenProvider;
import com.kiwi.ApiServer.Service.FileStorageService;
import com.kiwi.ApiServer.Table.interview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GetController {

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    ApplicationContext context;

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/user/test")
    public String test(){
//        Environment env = context.getEnvironment();
//        System.out.println(env.getProperty("spring.datasource.url"));

        return "success";
    }

    @GetMapping("/getInterview")
    public SingleResult getInterview(HttpServletRequest request) throws Exception{
        SingleResult res = new SingleResult();
        List<interview> data = new ArrayList<>();
        Environment env = context.getEnvironment();

        SQLDAO sqldao = new SQLDAO();

        String token = request.getHeader("X-AUTH-TOKEN");
        System.out.println(token);
        String user = jwtTokenProvider.getUser(token);
        System.out.println(user);

        ResultSet interview_list = sqldao.getInterviewList(user);
        while(interview_list.next()){
            String interviewId = interview_list.getString("interview_id");
            ResultSet interview_tmp = sqldao.getInterviewFromId(interviewId);
            while(interview_tmp.next()){
                interview tmp = new interview();
                tmp.setId(interview_tmp.getLong(1));
                tmp.setInterview_name(interview_tmp.getString(2));
                tmp.setStartDate(interview_tmp.getString(3));
                tmp.setStartTime(interview_tmp.getString(4));
                tmp.setTemplate(interview_tmp.getInt(5));
                data.add(tmp);
            }
        }
        res.setData(200);
        res.setMessage("success");
        res.setData(data);
        return res;
    }

    @GetMapping("/getUsername")
    public SingleResult getUsername(HttpServletRequest request) throws Exception{
        SingleResult result = new SingleResult();
        SQLDAO sqldao = new SQLDAO();
        String token = request.getHeader("X-AUTH-TOKEN");
        String email = jwtTokenProvider.getUser(token);

        ResultSet username = sqldao.getUsernameFromEmail(email);
        while(username.next()){
            result.setData(username.getString(1));
//            System.out.println(username.getString(1));
        }
        result.setResult(200);
        result.setMessage("success");
        return result;
    }

    @GetMapping("/getUser")
    public SingleResult getUser(HttpServletRequest request) throws Exception {
        SingleResult result = new SingleResult();
        SQLDAO sqldao = new SQLDAO();
        String token = request.getHeader("X-AUTH-TOKEN");
        String email = jwtTokenProvider.getUser(token);

       ResultSet userList = sqldao.getUserFromEmail(email);
       User user = new User();
       while(userList.next()){
           user.setName(userList.getString(1));
           user.setEmail(userList.getString(2));
           user.setMemberType(userList.getInt(3));
       }
       result.setResult(200);
       result.setMessage("success");
       result.setData(user);
        return result;
    }

    @GetMapping("/deleteInterview")
    public SingleResult deleteInterview(@RequestParam String id) throws Exception{
        SingleResult result = new SingleResult();
        SQLDAO sqldao = new SQLDAO();
        sqldao.deleteInterview(id);

        result.setResult(200);
        result.setMessage("success");
        return result;
    }

    @GetMapping("/participant")
    public SingleResult getParticipantList(@RequestParam String id) throws Exception {
        SingleResult result = new SingleResult();

        SQLDAO sqldao = new SQLDAO();
        List<InterviewParticipant> data = sqldao.getParticipantFromInterviewId(id);
        result.setResult(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    @GetMapping("/getEvaluationIdList")
    public SingleResult getEvaluationIdList(HttpServletRequest request) throws Exception{
        SingleResult result = new SingleResult();
        SQLDAO sqldao = new SQLDAO();

        String token = request.getHeader("X-AUTH-TOKEN");
        String email = jwtTokenProvider.getUser(token);
        int user_id = sqldao.getUserIdFromEmail(email);

        List<EvaluationIdName> evaluationIdList = sqldao.getEvaluationIdListFromUserId(user_id);

        result.setResult(200);
        result.setMessage("SUCCESS");
        result.setData(evaluationIdList);
        return result;
    }


    @GetMapping("/getEvaluation")
    public SingleResult getEvaluation(@RequestParam String evaluationId) throws Exception {
        SingleResult result = new SingleResult();
        SQLDAO sqldao = new SQLDAO();
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationList(new ArrayList<>());

        String name = sqldao.getNameFromEvaluation(evaluationId);
        evaluation.setName(name);

        List<EvaluationCategory> evaluationCategoryList = sqldao.getEvaluationCategoryFromId(evaluationId);
        for(EvaluationCategory evaluationCategory : evaluationCategoryList){
            boolean containsCategory = false;
            for(EvaluationList evaluationList : evaluation.getEvaluationList()){
                if(evaluationList.getCategory().equals(evaluationCategory.getCategory())){
                    containsCategory = true;

//                    EvaluationQuestion evaluationQuestion = new EvaluationQuestion();
                    GetEvaluationQuestion evaluationQuestion = new GetEvaluationQuestion();

                    evaluationQuestion.setTitle(evaluationCategory.getTitle());
                    evaluationQuestion.setType(evaluationCategory.getType());
                    evaluationQuestion.setRange(Integer.toString(evaluationCategory.getData()));
                    evaluationQuestion.setData(0);

//                    if(evaluationQuestion.getType() == 1){
//                        List<String> data = sqldao.getEvaluationChoiceFromQuestionId(evaluationCategory.getQuestion_id());
//                        evaluationQuestion.setData(data);
//                    }

                    evaluationList.getQuestions().add(evaluationQuestion);
                    break;
                }
            }
            if(!containsCategory){
                EvaluationList evaluationList = new EvaluationList();
                evaluationList.setQuestions(new ArrayList<>());
//                EvaluationQuestion evaluationQuestion = new EvaluationQuestion();
                GetEvaluationQuestion evaluationQuestion = new GetEvaluationQuestion();

                String category = evaluationCategory.getCategory();
                evaluationQuestion.setTitle(evaluationCategory.getTitle());
                evaluationQuestion.setType(evaluationCategory.getType());
                evaluationQuestion.setRange(Integer.toString(evaluationCategory.getData()));
                evaluationQuestion.setData(0);


//                if(evaluationQuestion.getType() == 1){
//                    List<String> data = sqldao.getEvaluationChoiceFromQuestionId(evaluationCategory.getQuestion_id());
//                    evaluationQuestion.setData(data);
//                }

                evaluationList.setCategory(category);
                evaluationList.getQuestions().add(evaluationQuestion);
                evaluation.getEvaluationList().add(evaluationList);
            }
        }
        result.setResult(200);
        result.setMessage("SUCCESS");
        result.setData(evaluation);
        return result;
    }

    @GetMapping("/deleteEvaluation")
    public SingleResult deleteEvaluation(@RequestParam int evaluationId) throws Exception{
        SingleResult result = new SingleResult();
        SQLDAO sqldao = new SQLDAO();

        sqldao.deleteEvaluationFromEvaluationId(evaluationId);

        result.setResult(200);
        result.setMessage("SUCCESS");
        return result;
    }

    @GetMapping("/getResume")
    public ResponseEntity<InputStreamResource> getResume(@RequestParam String name){
        String path = "./results/";
        String file_name = name + ".pdf";
        System.out.println(file_name);
        File file = new File(path + file_name);

        HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "inline;filename=" +file_name);
        InputStreamResource resource = null;
        try{
            resource = new InputStreamResource(new FileInputStream(file));
        }catch (Exception e){
            System.out.println(e);
        }
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(resource);
    }

    @GetMapping("/deleteResume")
    public SingleResult deleteResume(HttpServletRequest request, @RequestParam String name) throws Exception{
        SingleResult result = new SingleResult();
        String path = "./results/";
        String file_name = name+".pdf";
        File file = new File(path + file_name);

        if( file.exists() ){
            if(file.delete()){
                SQLDAO sqldao = new SQLDAO();
                String token = request.getHeader("X-AUTH-TOKEN");
                String email = jwtTokenProvider.getUser(token);
                int user_id = sqldao.getUserIdFromEmail(email);

                sqldao.deleteResume(user_id, file_name);
//                System.out.println("파일삭제 성공");
                result.setResult(200);
                result.setMessage("SUCCESS");
            }else{
//                System.out.println("파일삭제 실패");
                result.setResult(401);
                result.setMessage("FAILED");
            }
        } else{
//            System.out.println("파일이 존재하지 않습니다.");
            result.setResult(402);
            result.setMessage("FILE NOT EXIST ERROR");
        }

        return result;
    }

    @GetMapping("/getCreatedResumeList")
    public SingleResult getCreatedResumeList(HttpServletRequest request) throws Exception{
        SingleResult result = new SingleResult();
        SQLDAO sqldao = new SQLDAO();

        String token = request.getHeader("X-AUTH-TOKEN");
        String email = jwtTokenProvider.getUser(token);
        int user_id = sqldao.getUserIdFromEmail(email);

        List<String> data = sqldao.getResumeListFromUserId(user_id);

        result.setResult(200);
        result.setMessage("SUCCESS");
        result.setData(data);
        return result;
    }

    @GetMapping("/downloadInterviewResult/{name:.+}")
    public ResponseEntity<Resource> downloadResult(HttpServletRequest request, @PathVariable String name){
        String fileName = name + ".csv";

        Resource resource = fileStorageService.load(fileName);

        String contentType = null;
        try{
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException exception){
            System.out.println("Could not determine file Type");
        }

        if(contentType == null)
            contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/viewpdf")
    public ResponseEntity<InputStreamResource> getViewPdf() {
        System.out.println("connected");

        String path = "./uploads/";
        String file_name = "CV2.pdf";
        File file = new File(path + file_name);

        HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "inline;filename=" +file_name);
        InputStreamResource resource = null;
        try{
            resource = new InputStreamResource(new FileInputStream(file));
        }catch (Exception e){
            System.out.println(e);
        }
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(resource);
    }
}
