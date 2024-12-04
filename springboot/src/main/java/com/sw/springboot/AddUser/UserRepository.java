package com.sw.springboot.AddUser;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 이메일과 비밀번호로 사용자 조회
    Optional<User> findByEmailAndPassword(String email, String password);
}