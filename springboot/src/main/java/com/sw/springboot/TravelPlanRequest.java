package com.sw.springboot;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TravelPlanRequest {
    private String location; // "서울"
    private String startDate; // ISO 형식의 날짜 문자열
    private String endDate; // ISO 형식의 날짜 문자열
    private List<Map<String, String>> timeRanges; // [{ start: "09:00", end: "12:00" }, ...]
    private String[] selectedTags; // ["문화유산"]
    private String transportation; // "대중교통"
    private String[] selectedHotelTags;//호텔 성급 데이터
    private String accommodationAddress;//호텔 주소 데이터
}
