package com.sw.springboot.EmailVerification;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Controller
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/mailauth")
    public String mailauth() {
        return "mailauth.html"; // 인증 페이지 렌더링
    }

    // 인증 이메일 전송
    @PostMapping("/mailSend")
    public ResponseEntity<Boolean> mailSend(@RequestBody EmailForm mail) {
        System.out.println("mailSend 접속");
        HashMap<String, Object> map = new HashMap<>();

        try {
            emailVerificationService.sendMail(mail.getMail());
            System.out.println("전송에 성공하였습니다.");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            System.out.println("전송에 실패하였습니다-"+e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    // 인증번호 일치여부 확인
    @PostMapping("/mailCheck")
    public ResponseEntity<Boolean> mailCheck(@RequestBody checkEmailVerification checkEmailVerification) {
        System.out.println("mailCheck 접속");
        boolean isMatch = emailVerificationService.verifyNumber(checkEmailVerification.getMail(),checkEmailVerification.getEnteredVerificationCode());
        System.out.println(isMatch);
        return ResponseEntity.ok(isMatch);
    }
}
