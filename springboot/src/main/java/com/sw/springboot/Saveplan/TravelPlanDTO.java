package com.sw.springboot.Saveplan;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TravelPlanDTO {
    private Long id;
    private String date;
    private String dateStartTime;
    private String dateEndTime;
    private String location;
    private String weather;
    private int totalPrice;
    private HotelDataDTO hotelData;  // DTO 타입 사용
    private List<TotalSpotDTO> totalSpotList;
}