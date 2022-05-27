package com.kiwi.ApiServer.DTO.Evaluation;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
// Evaluation_category tbl FROM MYSQL
public class EvaluationCategory {
    int question_id;
    int evaluation_id;
    int type;
    String title;
    String category;
    int data;
}
