package com.sw.springboot.Saveplan;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class HotelDataDTO {
    private Long id;         // 호텔 데이터 고유 ID
    private String address;  // 호텔 주소
    private double lat;      // 위도
    private double lng;      // 경도
}
