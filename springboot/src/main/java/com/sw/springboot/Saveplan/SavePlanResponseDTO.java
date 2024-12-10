package com.sw.springboot.Saveplan;

import lombok.*;

import java.time.LocalDateTime;
@Setter
@Getter
public class SavePlanResponseDTO {

    private Long id;       // SavePlan의 ID
    private String saveName;  // 계획 이름
    private LocalDateTime createdAt;  // 생성일자

    public SavePlanResponseDTO(Long id, String saveName, LocalDateTime createdAt) {
        this.id = id;
        this.saveName = saveName;
        this.createdAt = createdAt;
    }
}
