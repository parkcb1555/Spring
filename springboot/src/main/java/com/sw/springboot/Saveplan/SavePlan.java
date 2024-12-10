package com.sw.springboot.Saveplan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class SavePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String saveName;


    private String userEmail;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 엔티티가 DB에 저장되기 전에 createdAt 값을 설정
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();  // 저장되기 전에 현재 시간으로 설정
    }

    // SavePlan이 여러 TravelPlan과 연결
    @OneToMany(mappedBy = "savePlan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TravelPlan> travelPlans;
}