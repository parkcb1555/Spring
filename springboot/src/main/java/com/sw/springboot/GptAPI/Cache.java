package com.sw.springboot.GptAPI;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gpt_cache")  // 테이블 이름을 명시적으로 지정
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ID는 고유값을 갖고 자동 증가

    @Column(nullable = false, length = 255)
    private String name;  // 요청 키워드

    @Column(length = 255)
    private String types;  // 유형 데이터

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;  // GPT API 응답 내용

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();  // 캐시 생성 시간

}
