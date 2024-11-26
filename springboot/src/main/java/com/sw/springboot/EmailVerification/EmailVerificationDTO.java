package com.sw.springboot.EmailVerification;

import lombok.Data;

public class EmailVerificationDTO {
    @Data
    public static class EmailRequest {
        private String email;
    }

    @Data
    public static class EmailVerificationRequest {
        private String email;
        private String code;
    }

}
