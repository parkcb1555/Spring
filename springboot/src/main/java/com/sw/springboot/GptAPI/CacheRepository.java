package com.sw.springboot.GptAPI;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CacheRepository extends JpaRepository<Cache, Long> {


    // name과 types로 Cache를 찾는 쿼리 메서드
    Optional<Cache> findByNameAndTypes(String name, String types);
}