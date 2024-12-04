package com.sw.springboot.EmailVerification;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final JavaMailSender javaMailSender;
    private static final String senderEmail = "your-email@gmail.com"; // 구글 이메일 설정

    // 각 사용자의 인증번호를 독립적으로 관리할 수 있는 HashMap
    private final HashMap<String, String> verificationCodes = new HashMap<>();

    // 랜덤으로 숫자 생성
    private String createVerificationNumber() {
        return String.valueOf((int)(Math.random() * (90000)) + 100000); // 6자리 랜덤 숫자
    }

    // 이메일을 위한 MIME 메시지 생성
    private MimeMessage createMail(String mail, String number) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail); // 발신 이메일 설정
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setSubject("이메일 인증");
            String body = "<h3>요청하신 인증 번호입니다.</h3>" +
                    "<h1>" + number + "</h1>" +
                    "<h3>유효 시간은 5분입니다.</h3>";
            message.setText(body, "UTF-8", "html");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }

    // 인증 이메일 보내기
    public String sendMail(String mail) {
        String number = createVerificationNumber();
        MimeMessage message = createMail(mail, number);
        javaMailSender.send(message);
        verificationCodes.put(mail, number); // 해당 이메일에 인증번호 저장
        return number;
    }

    // 사용자가 입력한 인증번호 검증
    public boolean verifyNumber(String mail, String userNumber) {
        String storedNumber = verificationCodes.get(mail);
        return storedNumber != null && storedNumber.equals(userNumber);
    }
}
