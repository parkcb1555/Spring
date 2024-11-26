package com.sw.springboot;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class RestMainController {

    @GetMapping("/api/data")
    public String test() {
        return "Hello React!";
    }

    @GetMapping("/api/testset")
    public String testset() {
        return "testset";
    }

    @PostMapping("/TravelSet1")
    public ResponseEntity<Object> TravelSet1(HttpSession session, @RequestBody Traveler traveler){
        System.out.println("TravelSet1 접근");
        System.out.println(traveler.getCity()+"/"+traveler.getStartdate()+"/"+traveler.getEnddate()+"/"+traveler.getPeoplecount());
        session.setAttribute("person", traveler);

        return ResponseEntity.ok(traveler);
    }

    // Person 객체를 포함한 데이터 받기
    @PostMapping("/TravelSet2")
    public ResponseEntity<Void> TravelSet2(HttpSession session, @RequestBody Traveler traveler){
        System.out.println("TravelSet2 접근");
        // 기본 데이터 출력
        System.out.println("도시: " + traveler.getCity());
        System.out.println("여행 시작일: " + traveler.getStartdate());
        System.out.println("여행 종료일: " + traveler.getEnddate());
        System.out.println("여행 인원수: " + traveler.getPeoplecount());

        // 일정 데이터 출력
        System.out.println("일정 데이터:");
        if (traveler.getSchedule() != null) {
            for (Period period : traveler.getSchedule()) {
                System.out.println("  날짜: " + period.getDate());
                System.out.println("  시작 시간: " + period.getStartTime());
                System.out.println("  종료 시간: " + period.getEndTime());
            }
        } else {
            System.out.println("일정 데이터가 없습니다.");
        }

        // 세션에 traveler 저장
        session.setAttribute("traveler", traveler);

        return ResponseEntity.ok().build(); // 응답 데이터 없음
    }


}

