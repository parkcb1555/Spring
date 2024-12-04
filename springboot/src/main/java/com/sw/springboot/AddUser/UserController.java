package com.sw.springboot.AddUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    // 회원가입 API
    @PostMapping("/register")
    public ResponseEntity<Boolean> register(@RequestBody UserDto userDto) {
        System.out.println("/register 접근");
        
        // 회원가입 처리
        try {
            userService.registerUser(userDto.getEmail(),userDto.getPassword());
            // 성공 시 true 반환
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            // 실패 시 false 반환
            return ResponseEntity.status(500).body(false);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Boolean> loginUser(@RequestBody UserDto userDto) {
        System.out.println("/login 접근");

        // 이메일과 비밀번호로 사용자 존재 여부 확인
        boolean logincheck = userService.findUser(userDto.getEmail(),userDto.getPassword());

        System.out.println(logincheck);

        return ResponseEntity.ok(logincheck);
    }
}
