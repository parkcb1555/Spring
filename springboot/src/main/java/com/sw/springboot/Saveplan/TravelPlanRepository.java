package com.sw.springboot.Saveplan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

    // 사용자 이메일로 플랜 조회
    @Query("SELECT tp FROM TravelPlan tp WHERE tp.savePlan.userEmail = :email")
    List<TravelPlan> findByUserEmail(@Param("email") String email);


    Optional<TravelPlan> findById(Long id);  // id로 특정 여행 계획을 조회
}