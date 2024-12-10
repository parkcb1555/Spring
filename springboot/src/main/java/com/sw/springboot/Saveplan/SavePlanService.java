package com.sw.springboot.Saveplan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SavePlanService {

    @Autowired
    private SavePlanRepository savePlanRepository;

    public List<SavePlan> getPlansByUserEmail(String userEmail) {
        return savePlanRepository.findByUserEmail(userEmail);
    }

}