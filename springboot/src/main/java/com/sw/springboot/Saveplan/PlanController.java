package com.sw.springboot.Saveplan;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class PlanController {

    private final TravelPlanService travelPlanService;
    @Autowired
    private SavePlanService savePlanService;

    public PlanController(TravelPlanService travelPlanService) {
        this.travelPlanService = travelPlanService;
    }




    @PostMapping("/saveTravelPlan")
    public String saveTravelPlan(@RequestBody SavePlan savePlan) {
        travelPlanService.saveTravelPlan(savePlan);
        return "Travel plan saved successfully!";
    }

    @PostMapping("/user")
    public ResponseEntity<List<SavePlanResponseDTO>> getSavePlansByEmail(@RequestBody Map<String, String> requestBody) {
        System.out.println("/user 접속");
        String email = requestBody.get("email"); // POST 요청으로 받은 이메일

        List<SavePlan> savePlans = savePlanService.getPlansByUserEmail(email);

        // SavePlan 리스트를 SavePlanResponseDTO 형식으로 변환
        List<SavePlanResponseDTO> response = savePlans.stream()
                .map(plan -> new SavePlanResponseDTO(plan.getId(), plan.getSaveName(), plan.getCreatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/myplan")
    public ResponseEntity<List<TravelPlanDTO>> getPlansWithDetails(@RequestBody Map<String, String> requestBody) {
        System.out.println("/myplan by id 접속");
        String email = requestBody.get("email");
        Long id = Long.valueOf(requestBody.get("id"));
        Optional<TravelPlan> travelPlans = travelPlanService.getPlanById(id);

        List<TravelPlanDTO> response = travelPlans.stream()
                .map(plan -> new TravelPlanDTO(
                        plan.getId(),
                        plan.getDate(),
                        plan.getDateStartTime(),
                        plan.getDateEndTime(),
                        plan.getLocation(),
                        plan.getWeather(),
                        plan.getTotalPrice(),
                        plan.getHotelData() != null ? new HotelDataDTO(
                                plan.getHotelData().getId(),
                                plan.getHotelData().getAddress(),
                                plan.getHotelData().getLat(),
                                plan.getHotelData().getLng()
                        ) : null,
                        plan.getTotalSpotList() != null ? plan.getTotalSpotList().stream()
                                .map(spot -> new TotalSpotDTO(
                                        spot.getId(),
                                        spot.getSpotName(),
                                        spot.getSpotLat(),
                                        spot.getSpotLng(),
                                        spot.getSpotStartTime(),
                                        spot.getSpotEndTime(),
                                        spot.getSpotDescription(),
                                        spot.getSpotPhoto(),
                                        spot.getSpotRating(),
                                        spot.getSpotTotaltips(),
                                        spot.getSearchPlacesDetail(),
                                        spot.getDirectionTime(),
                                        spot.getDirectionURL(),
                                        spot.getPrice()
                                ))
                                .collect(Collectors.toList()) : Collections.emptyList()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

//    @PostMapping("/myplan")
//    public ResponseEntity<List<TravelPlanDTO>> getPlansWithDetails(@RequestBody Map<String, String> requestBody) {
//        System.out.println("/myplan by email 접속");
//        String email = requestBody.get("email");
//        List<TravelPlan> travelPlans = travelPlanService.getPlansByUserEmail(email);
//
//        List<TravelPlanDTO> response = travelPlans.stream()
//                .map(plan -> new TravelPlanDTO(
//                        plan.getId(),
//                        plan.getDate(),
//                        plan.getDateStartTime(),
//                        plan.getDateEndTime(),
//                        plan.getLocation(),
//                        plan.getWeather(),
//                        plan.getHotelData() != null ? new HotelDataDTO(
//                                plan.getHotelData().getId(),
//                                plan.getHotelData().getAddress(),
//                                plan.getHotelData().getLat(),
//                                plan.getHotelData().getLng()
//                        ) : null,
//                        plan.getTotalSpotList() != null ? plan.getTotalSpotList().stream()
//                                .map(spot -> new TotalSpotDTO(
//                                        spot.getId(),
//                                        spot.getSpotName(),
//                                        spot.getSpotLat(),
//                                        spot.getSpotLng(),
//                                        spot.getSpotStartTime(),
//                                        spot.getSpotEndTime(),
//                                        spot.getSpotDescription(),
//                                        spot.getSpotPhoto(),
//                                        spot.getSpotRating(),
//                                        spot.getSpotTotaltips(),
//                                        spot.getSearchPlacesDetail(),
//                                        spot.getDirectionTime(),
//                                        spot.getDirectionURL()
//                                ))
//                                .collect(Collectors.toList()) : Collections.emptyList()
//                ))
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(response);
//    }
}