package com.sw.springboot.Saveplan;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
public class TotalSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne
    @JoinColumn(name = "travel_plan_id", nullable = false)
    private TravelPlan travelPlan;
}
