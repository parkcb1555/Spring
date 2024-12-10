package com.sw.springboot.Saveplan;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class TotalSpotDTO {
    private Long id;
    private String spotName;
    private double spotLat;
    private double spotLng;
    private String spotStartTime;
    private String spotEndTime;
    private String spotDescription;
    private String spotPhoto;
    private String spotRating;
    private String spotTotaltips;
    private String searchPlacesDetail;
    private int directionTime;
    private String directionURL;
    private int Price;
}