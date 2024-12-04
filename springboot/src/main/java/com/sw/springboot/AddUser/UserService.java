package com.sw.springboot.AddUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private BCryptPasswordEncoder passwordEncoder;

    // 회원가입 로직
    public void registerUser(String email, String password) {
        // 비밀번호 암호화
//        String encodedPassword = passwordEncoder.encode(password);

        // 사용자 객체 생성
        User user = User.builder()
                .email(email)
                .password(password)
                .build();

        // 사용자 저장
        userRepository.save(user);
    }

    public boolean findUser(String email, String password) {
        // 비밀번호 암호화
//        String encodedPassword = passwordEncoder.encode(password);
        Optional<User> userOptional = userRepository.findByEmailAndPassword(email,password);

//        User user = userOptional.get();
//        if (!PasswordUtil.matches(loginRequest.getPassword(), user.getPassword())) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 틀렸습니다.");
//        }

        return userOptional.isPresent();  // 사용자가 존재하면 true 반환, 아니면 false
    }

}