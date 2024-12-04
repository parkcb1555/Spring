package com.sw.springboot.AddUser;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")  // 테이블 이름을 명시적으로 지정
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID는 자동 증가
    private Long id;

    @Column(nullable = false, length = 255, unique = true)  // 이메일은 UNIQUE
    private String email;

    @Column(nullable = false, length = 255)  // 비밀번호 컬럼
    private String password;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 엔티티가 DB에 저장되기 전에 createdAt 값을 설정
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();  // 저장되기 전에 현재 시간으로 설정
    }
}
