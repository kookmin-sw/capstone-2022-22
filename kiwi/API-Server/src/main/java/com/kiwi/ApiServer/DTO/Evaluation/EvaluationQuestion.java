package com.kiwi.ApiServer.DTO.Evaluation;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class EvaluationQuestion {
    String title;
    int type;
//    List<String> data;
//    int range;
    String data;
}
