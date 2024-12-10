package com.sw.springboot.Saveplan;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// Getter & Setter
@Getter
@Setter
@Entity
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;
    private String dateStartTime;
    private String dateEndTime;
    private String location;
    private String weather;
    private int totalPrice;

    // 즉시 로딩
    @OneToOne(mappedBy = "travelPlan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private HotelData hotelData;

    // 즉시 로딩
    @OneToMany(mappedBy = "travelPlan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TotalSpot> totalSpotList;

    @ManyToOne
    @JoinColumn(name = "save_plan_id", nullable = false)
    private SavePlan savePlan;
}