package com.sw.springboot.EmailVerification;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/api/send-email-code")
    public ResponseEntity<String> sendEmailCode(@RequestBody EmailVerificationDTO.EmailRequest request) {
        emailVerificationService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok("Verification code sent.");
    }

    @PostMapping("/api/verify-email-code")
    public ResponseEntity<String> verifyEmailCode(@RequestBody EmailVerificationDTO.EmailVerificationRequest request) {
        boolean isVerified = emailVerificationService.verifyCode(request.getEmail(), request.getCode());
        if (isVerified) {
            return ResponseEntity.ok("Email verified successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid code.");
        }
    }
}
