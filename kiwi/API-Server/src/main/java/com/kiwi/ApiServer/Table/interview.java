package com.kiwi.ApiServer.Table;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String interview_name;

    @Column(length = 100, nullable = false)
    private String startDate;

    @Column(length = 100, nullable = false)
    private String startTime;

    @Column(nullable = false)
    private int template;
}
