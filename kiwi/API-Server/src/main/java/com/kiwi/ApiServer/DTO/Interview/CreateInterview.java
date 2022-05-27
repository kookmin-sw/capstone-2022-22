package com.kiwi.ApiServer.DTO.Interview;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@ToString
public class CreateInterview {
    private int interview_id;
    private String interviewName;
    private String startDate;
    private String startTime;
    private int template;
    private List<String> interviewee;
    private List<String> interviewer;
}
