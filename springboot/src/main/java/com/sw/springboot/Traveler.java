package com.sw.springboot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Traveler {
    private String city;
    private String startdate;
    private String enddate;
    private int peoplecount;

    private List<Period> schedule;
    // Getter, Setter, 생성자 등
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Period {
    private String date;
    private String startTime;
    private String endTime;
}

