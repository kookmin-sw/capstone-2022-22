package com.kiwi.ApiServer.DTO.Interview;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterviewParticipant {
    String email;
    String name;
    int member_type;
}
