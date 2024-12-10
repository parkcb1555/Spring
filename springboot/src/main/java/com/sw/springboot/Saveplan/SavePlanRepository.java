package com.sw.springboot.Saveplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavePlanRepository extends JpaRepository<SavePlan, Long> {
    // 이메일을 기반으로 SavePlan을 찾는 메서드 추가
    List<SavePlan> findByUserEmail(String userEmail);
}