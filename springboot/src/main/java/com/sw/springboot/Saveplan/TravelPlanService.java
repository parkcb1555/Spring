package com.sw.springboot.Saveplan;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class TravelPlanService {

    private final SavePlanRepository savePlanRepository;

    private final TravelPlanRepository travelPlanRepository;


    public TravelPlanService(SavePlanRepository savePlanRepository,TravelPlanRepository travelPlanRepository) {
        this.savePlanRepository = savePlanRepository;
        this.travelPlanRepository = travelPlanRepository;
    }


    public List<TravelPlan> getPlansByUserEmail(String email) {
        return travelPlanRepository.findByUserEmail(email);
    }

    public Optional<TravelPlan> getPlanById(Long planId) {
        return travelPlanRepository.findById(planId);
    }

    @Transactional
    public SavePlan saveTravelPlan(SavePlan savePlan) {

        // TravelPlan과 SavePlan 연결 설정
        savePlan.getTravelPlans().forEach(travelPlan -> {

            // 부모 연결
            travelPlan.setSavePlan(savePlan);

            // HotelData와 TravelPlan 연결
            if (travelPlan.getHotelData() != null) {
                travelPlan.getHotelData().setTravelPlan(travelPlan); // 필수 연결
            }

            // TotalSpot과 TravelPlan 연결
            travelPlan.getTotalSpotList().forEach(spot -> spot.setTravelPlan(travelPlan));
        });

        return savePlanRepository.save(savePlan); // 전체 저장
    }

}
