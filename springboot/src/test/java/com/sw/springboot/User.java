package com.sw.springboot;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDate;

@Entity
@Table(name="User") // 미 사용시 클래스이름 == 테이블이름
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(length=128)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(length=128)
    private String user_name;

    @Column(length=128)
    private String country;

    @Column(length=32)
    private String tel;

    @Column
    private LocalDate birth;

}