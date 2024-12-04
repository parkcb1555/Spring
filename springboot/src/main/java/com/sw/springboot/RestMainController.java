package com.sw.springboot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.model.*;
import com.sw.springboot.ChooseTravelSpot.RecommendationScore;
import com.sw.springboot.GooGlePlaceAPI.DirectionsService;
import com.sw.springboot.GooGlePlaceAPI.GGP_Controller;
import com.sw.springboot.GooGlePlaceAPI.GGP_Service;
import com.sw.springboot.WeatherAPI.weather;
import com.sw.springboot.WeatherAPI.WeatherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class RestMainController {
    @Autowired
    RecommendationScore recommendationScore;

    @Autowired
    GGP_Service ggp_service;

    @Autowired
    DirectionsService directionsService;

    @Autowired
    WeatherService weatherService;



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


    public String VilageWeatherSet(LocalDate nowdate,Map<LocalDate, Double> VilageWeatherData){
        String weatherDescription = "";
        Boolean weatherCheck = false;
        for (Map.Entry<LocalDate, Double> entry : VilageWeatherData.entrySet()) {
            LocalDate date = entry.getKey();
            Double value = entry.getValue();
            if (weatherCheck){
                break;
            }

            if(date.equals(nowdate)) {
                if (value >= 0.0 && value < 3.0) {
                    weatherDescription = "맑음";
                    weatherCheck = true;
                    System.out.println("value : "+value+">>"+date+"에는 날씨가 "+weatherDescription+" 입니다.");
                    break;
                } else if (value >= 3.0 && value < 4.0) {
                    weatherDescription = "구름많음";
                    weatherCheck = true;
                    System.out.println("value : "+value+">>"+date+"에는 날씨가 "+weatherDescription+" 입니다.");
                    break;
                } else if (value >= 4.0) {
                    weatherDescription = "흐림";
                    weatherCheck = true;
                    System.out.println("value : "+value+">>"+date+"에는 날씨가 "+weatherDescription+" 입니다.");
                    break;
                } else {
                    weatherDescription = "알 수 없음"; // 범위 외 값 처리
                    weatherCheck = true;
                    System.out.println("value : "+value+">>"+date+"에는 날씨가 "+weatherDescription+" 입니다.");
                    break;
                }
            }
        }
        return weatherDescription;
    }

    public String MidWeatherSet(LocalDate startdate,LocalDate nowdate,String MidWeatherResult){
        String result = "";
        long daysBetween = ChronoUnit.DAYS.between(startdate, nowdate);
        System.out.println("날짜 차이: " + daysBetween + "일");

        try {
            // Parse the JSON response using Gson
            JsonObject jsonObject = JsonParser.parseString(MidWeatherResult).getAsJsonObject();
            JsonArray items = jsonObject.getAsJsonObject("response")
                    .getAsJsonObject("body")
                    .getAsJsonObject("items")
                    .getAsJsonArray("item");

            JsonObject item = items.get(0).getAsJsonObject(); // 첫 번째 객체 추출
            String[] keys = { "wf3Am", "wf3Pm", "wf4Am", "wf4Pm", "wf5Am", "wf5Pm",
                    "wf6Am", "wf6Pm", "wf7Am", "wf7Pm", "wf8", "wf9", "wf10" };

            // daysBetween에 따른 키 선택
            String key = "";
            if (daysBetween == 3) {
                key = "wf3Am";
            } else if (daysBetween == 4) {
                key = "wf4Am";
            } else if (daysBetween == 5) {
                key = "wf5Am";
            } else if (daysBetween == 6) {
                key = "wf6Am";
            } else if (daysBetween == 7) {
                key = "wf7Am";
            } else if (daysBetween == 8) {
                key = "wf8";
            } else if (daysBetween == 9) {
                key = "wf9";
            } else if (daysBetween == 10) {
                key = "wf10";
            }

            // 키 존재 여부 확인 및 처리
            if (!key.isEmpty()) { // 유효한 키가 있을 경우
                if (item.has(key)) {
                    result = item.get(key).getAsString(); // 키가 존재하면 값 설정
                    System.out.println("날씨 데이터: " + result);
                } else {
                    result = "맑음"; // 키가 없으면 기본값 설정
                    System.out.println("키 " + key + "가 없습니다. 기본값 '맑음'을 설정합니다.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public String extractTime(LocalDateTime input) {
        // 원하는 출력 형식: HH:mm
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return input.format(formatter);
    }


    public List<Map<String, Object>> MakePlan(TravelPlanRequest request) throws Exception {
        //최종적으로 클라이언트로 전달할 리스트
        List<Map<String, Object>> travelPlans = new ArrayList<>();
        System.out.println("MakePlan 접근");


        for (String tag : request.getSelectedTags()) {
            System.out.println("선택된 태그: " + tag);
        }

        String city = request.getLocation();
        String startdate = request.getStartDate();
        String enddate = request.getEndDate();


        // 시작 날짜와 종료 날짜 범위 확인
        LocalDate start = LocalDate.parse(startdate);
        LocalDate end = LocalDate.parse(enddate);

        // 날짜와 시간 데이터를 저장할 Map 생성
        Map<String, String[]> dateTimeMap = new LinkedHashMap<>();

        List<Map<String, String>> timeRanges = request.getTimeRanges();
        if (timeRanges != null) {
            LocalDate firstday = LocalDate.parse(startdate);
            for (Map<String, String> timeRange : timeRanges) {
                String startTime = timeRange.get("start"); // start 값 추출
                String endTime = timeRange.get("end");    // end 값 추출

                // 날짜와 시작, 종료 시간을 배열로 저장
                dateTimeMap.put(firstday.toString(), new String[] { startTime, endTime });


                // 다음 날짜로 증가
                firstday = firstday.plusDays(1);
            }
        }

        //기상청 날씨 api
        weather weather = new weather();
        Map<LocalDate, Double> VilageWeatherData = Map.of();
        String MidWeather="";

        //기상청 api 단기예보 호출
        VilageWeatherData = weather.VilageFcstInfoService(weatherService.printApiKey(),LocalDate.parse(startdate),LocalDate.parse(enddate));

        if(LocalDate.parse(enddate).isAfter(LocalDate.parse(startdate).plusDays(2))) {
            //기상청 api 중기예보 호출
            MidWeather = weather.MidFcstInfoService(weatherService.printApiKey(),LocalDate.parse(startdate),LocalDate.parse(enddate));
        }

        Map<LocalDateTime, JsonArray> result = recommendationScore.ChooseTravelSpot(city,request.getSelectedTags(),startdate,enddate,dateTimeMap,directionsService.getKey());

        // JsonArray에서 필요한 데이터를 추출
        Map<LocalDateTime, List<Map<String, String>>> processedResult = new LinkedHashMap<>();


        double Centerlatitude = 0;
        double Centerlongitude = 0;
        int spotcount = 0;


        for (Map.Entry<LocalDateTime, JsonArray> entry : result.entrySet()) {
            JsonArray spots = entry.getValue();
            if (!spots.isEmpty()) {
                // 첫 번째 장소 가져오기
                JsonObject firstSpot = spots.get(0).getAsJsonObject().getAsJsonObject("geocodes").getAsJsonObject("main");

                // 위도와 경도 추출
                double latitude = firstSpot.get("latitude").getAsDouble();
                double longitude = firstSpot.get("longitude").getAsDouble();

                // 합계에 더하기
                Centerlatitude += latitude;
                Centerlongitude += longitude;
                spotcount++;
            }
        }

        Centerlatitude = Centerlatitude/spotcount;
        Centerlongitude = Centerlongitude/spotcount;

        // 호텔 호출
        GGP_Controller ggp_controller = new GGP_Controller(ggp_service);
        PlacesSearchResponse PlacesSearchresponse =ggp_controller.getNearbyPlaces(Centerlatitude,Centerlongitude,"LODGING");

        //여행 플랜 제작
        for (Map.Entry<LocalDateTime, JsonArray> entry : result.entrySet()) {
            //여행 플랜 저장
            Map<String, Object> Plan = new LinkedHashMap<>();
            //완성된 일자별 여행지 계획 저장
            List<Map<String, Object>> TotalSpotList = new ArrayList<>();
            
            LocalDateTime time = entry.getKey();
            LocalDate nowdate = time.toLocalDate();
            JsonArray spots = entry.getValue();  // entry에서 spots 배열 추출
            Boolean lunchtime = false;
            Boolean dinertime = false;

            //날짜 데이터 삽입
            Plan.put("Date",nowdate);

            //도시 삽입
            Plan.put("location",city);

            if(nowdate.isAfter(LocalDate.parse(startdate).plusDays(2))) {
                String nowWeather = MidWeatherSet(LocalDate.parse(startdate),nowdate,MidWeather);
                //날씨 데이터 삽입
                Plan.put("Weather",nowWeather);
            }else{
                String nowWeather = VilageWeatherSet(nowdate, VilageWeatherData);
                //날씨 데이터 삽입
                Plan.put("Weather",nowWeather);
            }

            Plan.put("DateStartTime",extractTime(time));

            // spots 배열에서 순차적으로 각 spot 처리
            for (int i = 0; i < spots.size(); i++) {
                //여행지 일정 저장
                Map<String, Object> SpotList = new LinkedHashMap<>();
                Map<String, Object> HotelData = new LinkedHashMap<>();
                JsonObject spot = spots.get(i).getAsJsonObject();  // 각 spot을 JsonObject로 변환


                long RegularTime = 0;
                int nowtime = time.getHour();
                int Endhour = 0;


                // dateTimeMap에 저장된 각 날짜별 시작 시간과 종료 시간 출력
                for (Map.Entry<String, String[]> DateTimeentry : dateTimeMap.entrySet()) {
                    String date = DateTimeentry.getKey(); // 날짜 (key)
                    String[] times = DateTimeentry.getValue(); // 시작 시간과 종료 시간 (value)

                    String startTime = times[0];
                    String endTime = times[1];

                    // String을 LocalTime으로 변환
                    LocalTime hourendtime = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
                    if(LocalDate.parse(date).equals(nowdate)) {

                        // getHour() 메서드로 시간 추출
                        Endhour = hourendtime.getHour();

                        break;
                    }
                }


                if(nowtime>=Endhour){
                    System.out.println(nowtime+">="+Endhour);
                    System.out.println(spots.get(i-1).getAsJsonObject().get("name").getAsString());
                    JsonObject currentSpot = spots.get(i-1).getAsJsonObject().getAsJsonObject("geocodes").getAsJsonObject("main");
                    double currentLat = currentSpot.get("latitude").getAsDouble();
                    double currentLng = currentSpot.get("longitude").getAsDouble();



                    PlacesSearchResult BestHotel = PlacesSearchresponse.results[0];

                    //길찾기-호텔 호출
//                    DirectionsResult route = directionsService.getDirections(currentLat, currentLng, BestHotel.geometry.location.lat, BestHotel.geometry.location.lng);
                    DirectionsResult route = directionsService.getNameDirections(spots.get(i-1).getAsJsonObject().get("name").getAsString(), BestHotel.name);


                    PlaceDetails placeDetails = ggp_controller.searchPlacesDetail(BestHotel.placeId);

                    HotelData = new LinkedHashMap<>();
                    HotelData.put("Address",placeDetails.formattedAddress);
                    HotelData.put("Lat",BestHotel.geometry.location.lat);
                    HotelData.put("Lng",BestHotel.geometry.location.lng);



                    // 총 이동 시간을 담을 변수 (초 단위로 저장)
                    long totalDurationInSeconds = 0;

                    // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                    for (DirectionsLeg leg : route.routes[0].legs) {
                        // 각 leg에서 duration을 가져와 총 시간에 더함
                        Duration duration = leg.duration;
                        totalDurationInSeconds += duration.inSeconds;
                    }

                    // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                    long totalDurationInMinutes = totalDurationInSeconds / 60;

//                    SpotList.put("SpotName",spots.get(i-1).getAsJsonObject().get("name").getAsString());
//                    SpotList.put("SpotLat",currentLat);
//                    SpotList.put("SpotLng",currentLng);
//                    SpotList.put("SpotTotaltips",spots.get(i-1).getAsJsonObject().getAsJsonObject("stats").get("total_tips").getAsString());
//                    SpotList.put("SpotRating",spots.get(i-1).getAsJsonObject().get("rating").getAsString());
//                    SpotList.put("SpotDescription",spots.get(i-1).getAsJsonObject().get("PlaceDescription").getAsString());
//                    SpotList.put("SpotPhoto",spots.get(i-1).getAsJsonObject().get("photourl").getAsString());
//                    SpotList.put("DirectionTime",totalDurationInMinutes);
//                    SpotList.put("DirectionURL","https://www.google.com/maps/dir/?api=1&origin="+currentLat+","+currentLng+"&destination="+BestHotel.geometry.location.lat+","+BestHotel.geometry.location.lng+"&travelmode=transit");
//                    SpotList.put("searchPlacesDetail","https://www.google.com/maps/search/"+spot.get("name").getAsString());
//
//                    Plan.put("SpotPlan",SpotList);

                    Plan.put("HotelData",HotelData);
                    break;
                }

                System.out.println();
                System.out.println("진행중");
                System.out.println();

                if ( i < spots.size()-1){
                    double currentLat;
                    double currentLng;
                    double nextLat;
                    double nextLng;

                    JsonObject currentSpot = spots.get(i).getAsJsonObject().getAsJsonObject("geocodes").getAsJsonObject("main");
                    JsonObject nextSpot = spots.get(i + 1).getAsJsonObject().getAsJsonObject("geocodes").getAsJsonObject("main");
                    currentLat = currentSpot.get("latitude").getAsDouble();
                    currentLng = currentSpot.get("longitude").getAsDouble();
                    nextLat = nextSpot.get("latitude").getAsDouble();
                    nextLng = nextSpot.get("longitude").getAsDouble();
                    RegularTime = Long.parseLong(spot.get("RegularTime").getAsString());


                    // 총 이동 시간을 담을 변수 (초 단위로 저장)
                    long totalDurationInSeconds = 0;
                    System.out.println("장소 추가!!!!!!!!!!!!!");
                    System.out.println(spot.get("name").getAsString());

                    JsonObject stats = spot.getAsJsonObject("stats");  // stats 객체 추출

                    ////여행지 데이터 저장-여행지 시작 시간
                    SpotList.put("SpotStartTime",extractTime(time));
                    //진행중인 시간 + 관람시간
                    time = time.plusMinutes(RegularTime);
                    ////여행지 데이터 저장-여행지 종료 시간
                    SpotList.put("SpotEndTime",extractTime(time));

                    //오후 12시 이상이 되어 점심 시간이 된 경우
                    if (time.getHour() >= 12 && !lunchtime){
                        //현재 여행지 -> 점심식사 장소
                        System.out.println("점심시간!!!!!!!!!!");

                        JsonObject Restaurant = recommendationScore.ChooseRestaurant(currentLat,currentLng,directionsService.getKey());
                        double RestaurantLat = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("latitude").getAsDouble();
                        double RestaurantLng = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("longitude").getAsDouble();

                        System.out.println(Restaurant.getAsJsonObject().get("name").getAsString());
//                        DirectionsResult route = directionsService.getDirections(currentLat, currentLng, RestaurantLat, RestaurantLng);
                        DirectionsResult route = directionsService.getNameDirections(
                                spots.get(i).getAsJsonObject().get("name").getAsString(),
                                Restaurant.getAsJsonObject().get("name").getAsString());

                        // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                        for (DirectionsLeg leg : route.routes[0].legs) {
                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }

                        // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                        long totalDurationInMinutes = totalDurationInSeconds / 60;
                        String SpotTotaltips =
                                String.valueOf((spot.getAsJsonObject("stats").get("total_tips").getAsInt()
                                        +spot.getAsJsonObject("stats").get("total_ratings").getAsInt()
                                        +spot.getAsJsonObject("stats").get("total_photos").getAsInt()));

                        SpotList.put("SpotName",spot.get("name").getAsString());
                        SpotList.put("SpotLat",currentLat);
                        SpotList.put("SpotLng",currentLng);
                        SpotList.put("SpotTotaltips",SpotTotaltips);
                        SpotList.put("SpotRating",spot.get("rating").getAsString());
                        SpotList.put("SpotDescription",spot.get("PlaceDescription").getAsString());
                        SpotList.put("SpotPhoto",spot.get("photourl").getAsString());
                        SpotList.put("DirectionTime",totalDurationInMinutes);
                        SpotList.put("DirectionURL","https://www.google.com/maps/dir/?api=1&origin="+currentLat+","+currentLng+"&destination="+RestaurantLat+","+RestaurantLng+"&travelmode=transit");
                        SpotList.put("searchPlacesDetail","https://www.google.com/maps/search/"+spot.get("name").getAsString());

                        time = time.plusMinutes(totalDurationInMinutes);

                        TotalSpotList.add(SpotList);


                        //점심식사 장소 -> 다음 여행지
                        SpotList = new LinkedHashMap<>();


//                        route = directionsService.getDirections(RestaurantLat, RestaurantLng,nextLat,nextLng);
                        route = directionsService.getNameDirections(Restaurant.getAsJsonObject().get("name").getAsString()
                                ,spots.get(i+1).getAsJsonObject().get("name").getAsString());


                        // 총 이동 시간을 담을 변수 (초 단위로 저장)
                        totalDurationInSeconds = 0;

                        // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                        for (DirectionsLeg leg : route.routes[0].legs) {
                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }
                        // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                        totalDurationInMinutes = totalDurationInSeconds / 60;

                        PlacesSearchResult BestHotel = PlacesSearchresponse.results[0];
                        RegularTime = Long.parseLong(Restaurant.get("RegularTime").getAsString());

                        ////여행지 데이터 저장-여행지 시작 시간
                        SpotList.put("SpotStartTime",extractTime(time));
                        //진행중인 시간 + 관람시간
                        time = time.plusMinutes(RegularTime);
                        ////여행지 데이터 저장-여행지 종료 시간
                        SpotList.put("SpotEndTime",extractTime(time));

                        if(time.plusMinutes(totalDurationInMinutes).getHour() >= Endhour){
                            route = directionsService.getNameDirections(Restaurant.getAsJsonObject().get("name").getAsString()
                                    ,BestHotel.name);
                            // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                            for (DirectionsLeg leg : route.routes[0].legs) {
                                // 각 leg에서 duration을 가져와 총 시간에 더함
                                Duration duration = leg.duration;
                                totalDurationInSeconds += duration.inSeconds;
                            }
                            totalDurationInMinutes = totalDurationInSeconds / 60;
                        }

                        SpotTotaltips =
                                String.valueOf((Restaurant.getAsJsonObject("stats").get("total_tips").getAsInt()
                                        +Restaurant.getAsJsonObject("stats").get("total_ratings").getAsInt()
                                        +Restaurant.getAsJsonObject("stats").get("total_photos").getAsInt()));

                        System.out.println(Restaurant.get("name").getAsString());
                        SpotList.put("SpotName",Restaurant.get("name").getAsString());
                        SpotList.put("SpotLat",RestaurantLat);
                        SpotList.put("SpotLng",RestaurantLng);
                        SpotList.put("SpotTotaltips",SpotTotaltips);
                        SpotList.put("SpotRating",Restaurant.get("rating").getAsString());
                        SpotList.put("SpotDescription",Restaurant.get("PlaceDescription").getAsString());
                        SpotList.put("SpotPhoto",Restaurant.get("photourl").getAsString());
                        SpotList.put("DirectionTime",totalDurationInMinutes);
                        if(time.plusMinutes(totalDurationInMinutes).getHour() >= Endhour){
                            SpotList.put("DirectionURL","https://www.google.com/maps/dir/?api=1&origin="+RestaurantLat+","+RestaurantLng+"&destination="+BestHotel.geometry.location.lat+","+BestHotel.geometry.location.lng+"&travelmode=transit");
                        }else{
                            SpotList.put("DirectionURL","https://www.google.com/maps/dir/?api=1&origin="+RestaurantLat+","+RestaurantLng+"&destination="+nextLat+","+nextLng+"&travelmode=transit");
                        }
                        SpotList.put("searchPlacesDetail","https://www.google.com/maps/search/"+Restaurant.get("name").getAsString());

                        time = time.plusMinutes(totalDurationInMinutes);
                        System.out.println("점심식사 이후 시간>>"+time);
                        lunchtime = true;
                        System.out.println("점심시간 종료!!!!!!!!");
                    }
                    //18시 이상이 되어 저녁 시간이 된 경우
                    else if (nowtime >= 18 && !dinertime) {
                        System.out.println("저녁시간!!!!!!!!!!");
                        System.out.println(time.getHour());

                        System.out.println("Dinertime : "+currentLat+"   "+currentLng);

                        JsonObject Restaurant = recommendationScore.ChooseRestaurant(currentLat,currentLng,directionsService.getKey());
                        double RestaurantLat = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("latitude").getAsDouble();
                        double RestaurantLng = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("longitude").getAsDouble();

                        System.out.println(currentLat+"   "+currentLng+"   "+RestaurantLat+"   "+RestaurantLng);
//                        DirectionsResult route = directionsService.getDirections(currentLat, currentLng, RestaurantLat, RestaurantLng);
                        DirectionsResult route = directionsService.getNameDirections(spots.get(i).getAsJsonObject().get("name").getAsString(),
                                Restaurant.getAsJsonObject().get("name").getAsString());

                        // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                        for (DirectionsLeg leg : route.routes[0].legs) {
                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }

                        // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                        long totalDurationInMinutes = totalDurationInSeconds / 60;

                        String SpotTotaltips =
                                String.valueOf((spot.getAsJsonObject("stats").get("total_tips").getAsInt()
                                        +spot.getAsJsonObject("stats").get("total_ratings").getAsInt()
                                        +spot.getAsJsonObject("stats").get("total_photos").getAsInt()));

                        SpotList.put("SpotName",spot.get("name").getAsString());
                        SpotList.put("SpotLat",currentLat);
                        SpotList.put("SpotLng",currentLng);
                        SpotList.put("SpotTotaltips",SpotTotaltips);
                        SpotList.put("SpotRating",spot.get("rating").getAsString());
                        SpotList.put("SpotDescription",spot.get("PlaceDescription").getAsString());
                        SpotList.put("SpotPhoto",spot.get("photourl").getAsString());
                        SpotList.put("DirectionTime",totalDurationInMinutes);
                        SpotList.put("DirectionURL","https://www.google.com/maps/dir/?api=1&origin="+currentLat+","+currentLng+"&destination="+RestaurantLat+","+RestaurantLng+"&travelmode=transit");
                        SpotList.put("searchPlacesDetail","https://www.google.com/maps/search/"+spot.get("name").getAsString());

                        time = time.plusMinutes(totalDurationInMinutes);

                        TotalSpotList.add(SpotList);


                        //점심식사 장소 -> 다음 여행지
                        SpotList = new LinkedHashMap<>();


//                        route = directionsService.getDirections(RestaurantLat, RestaurantLng,nextLat,nextLng);
                        route = directionsService.getNameDirections(Restaurant.getAsJsonObject().get("name").getAsString()
                                ,spots.get(i+1).getAsJsonObject().get("name").getAsString());

                        // 총 이동 시간을 담을 변수 (초 단위로 저장)
                        totalDurationInSeconds = 0;

                        // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                        for (DirectionsLeg leg : route.routes[0].legs) {
                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }
                        // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                        totalDurationInMinutes = totalDurationInSeconds / 60;
                        PlacesSearchResult BestHotel = PlacesSearchresponse.results[0];


                        RegularTime = Long.parseLong(Restaurant.get("RegularTime").getAsString());

                        ////여행지 데이터 저장-여행지 시작 시간
                        SpotList.put("SpotStartTime",extractTime(time));
                        //진행중인 시간 + 관람시간
                        time = time.plusMinutes(RegularTime);
                        ////여행지 데이터 저장-여행지 종료 시간
                        SpotList.put("SpotEndTime",extractTime(time));

                        if(time.plusMinutes(totalDurationInMinutes).getHour() >= Endhour){
                            route = directionsService.getNameDirections(Restaurant.getAsJsonObject().get("name").getAsString(),BestHotel.name);
                            // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                            for (DirectionsLeg leg : route.routes[0].legs) {
                                // 각 leg에서 duration을 가져와 총 시간에 더함
                                Duration duration = leg.duration;
                                totalDurationInSeconds += duration.inSeconds;
                            }
                            totalDurationInMinutes = totalDurationInSeconds / 60;
                        }

                        SpotTotaltips =
                                String.valueOf((Restaurant.getAsJsonObject("stats").get("total_tips").getAsInt()
                                        +Restaurant.getAsJsonObject("stats").get("total_ratings").getAsInt()
                                        +Restaurant.getAsJsonObject("stats").get("total_photos").getAsInt()));

                        System.out.println(Restaurant.get("name").getAsString());
                        SpotList.put("SpotName",Restaurant.get("name").getAsString());
                        SpotList.put("SpotLat",RestaurantLat);
                        SpotList.put("SpotLng",RestaurantLng);
                        SpotList.put("SpotTotaltips",SpotTotaltips);
                        SpotList.put("SpotRating",Restaurant.get("rating").getAsString());
                        SpotList.put("SpotDescription",Restaurant.get("PlaceDescription").getAsString());
                        SpotList.put("SpotPhoto",Restaurant.get("photourl").getAsString());
                        SpotList.put("DirectionTime",totalDurationInMinutes);
                        if(time.plusMinutes(totalDurationInMinutes).getHour() >= Endhour){
                            SpotList.put("DirectionURL","https://www.google.com/maps/dir/?api=1&origin="+RestaurantLat+","+RestaurantLng+"&destination="+BestHotel.geometry.location.lat+","+BestHotel.geometry.location.lng+"&travelmode=transit");

                        }else{
                            SpotList.put("DirectionURL","https://www.google.com/maps/dir/?api=1&origin="+RestaurantLat+","+RestaurantLng+"&destination="+nextLat+","+nextLng+"&travelmode=transit");
                        }                        SpotList.put("searchPlacesDetail","https://www.google.com/maps/search/"+Restaurant.get("name").getAsString());

                        time = time.plusMinutes(totalDurationInMinutes);

                        System.out.println("저녁식사 이후 시간>>"+time);
                        dinertime = true;
                        System.out.println("저녁 종료!!!!!!!!");
                    }
                    else{
//                        DirectionsResult route = directionsService.getDirections(currentLat, currentLng, nextLat, nextLng);
                        System.out.println(spots.get(i).getAsJsonObject().get("name").getAsString()+">>>>>"+spots.get(i+1).getAsJsonObject().get("name").getAsString());
                        DirectionsResult route = directionsService.getNameDirections(spots.get(i).getAsJsonObject().get("name").getAsString(), spots.get(i+1).getAsJsonObject().get("name").getAsString());

                        // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                        for (DirectionsLeg leg : route.routes[0].legs) {
                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }

                        // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                        long totalDurationInMinutes = totalDurationInSeconds / 60;

                        PlacesSearchResult BestHotel = PlacesSearchresponse.results[0];
                        if(time.plusMinutes(totalDurationInMinutes).getHour() >= Endhour){
                            route = directionsService.getNameDirections(spots.get(i).getAsJsonObject().get("name").getAsString(),BestHotel.name);
                            // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                            for (DirectionsLeg leg : route.routes[0].legs) {
                                // 각 leg에서 duration을 가져와 총 시간에 더함
                                Duration duration = leg.duration;
                                totalDurationInSeconds += duration.inSeconds;
                            }
                            totalDurationInMinutes = totalDurationInSeconds / 60;
                        }

                        String SpotTotaltips =
                                String.valueOf((spot.getAsJsonObject("stats").get("total_tips").getAsInt()
                                        +spot.getAsJsonObject("stats").get("total_ratings").getAsInt()
                                        +spot.getAsJsonObject("stats").get("total_photos").getAsInt()));

                        SpotList.put("SpotName",spot.get("name").getAsString());
                        SpotList.put("SpotLat",currentLat);
                        SpotList.put("SpotLng",currentLng);
                        SpotList.put("SpotTotaltips",SpotTotaltips);
                        SpotList.put("SpotRating",spot.get("rating").getAsString());
                        SpotList.put("SpotDescription",spot.get("PlaceDescription").getAsString());
                        SpotList.put("SpotPhoto",spot.get("photourl").getAsString());
                        SpotList.put("DirectionTime",totalDurationInMinutes);
                        

                        if(time.plusMinutes(totalDurationInMinutes).getHour() >= Endhour){
                            SpotList.put("DirectionURL","https://www.google.com/maps/dir/?api=1&origin="+currentLat+","+currentLng+"&destination="+BestHotel.geometry.location.lat+","+BestHotel.geometry.location.lng+"&travelmode=transit");
                        }else{
                            SpotList.put("DirectionURL","https://www.google.com/maps/dir/?api=1&origin="+currentLat+","+currentLng+"&destination="+nextLat+","+nextLng+"&travelmode=transit");
                        }
                        SpotList.put("searchPlacesDetail","https://www.google.com/maps/search/"+spot.get("name").getAsString());

                        time = time.plusMinutes(totalDurationInMinutes);

                    }

                }
                else{ //일자별로 가장 마지막 장소
                    RegularTime = Long.parseLong(spot.get("RegularTime").getAsString());

                    JsonObject stats = spot.getAsJsonObject("stats");  // stats 객체 추출
                    JsonObject currentSpot = spots.get(i).getAsJsonObject().getAsJsonObject("geocodes").getAsJsonObject("main");
                    double currentLat = currentSpot.get("latitude").getAsDouble();
                    double currentLng = currentSpot.get("longitude").getAsDouble();

                    PlacesSearchResult BestHotel = PlacesSearchresponse.results[0];

                    ////여행지 데이터 저장-여행지 시작 시간
                    SpotList.put("SpotStartTime",extractTime(time));
                    //진행중인 시간 + 관람시간
                    time = time.plusMinutes(RegularTime);
                    ////여행지 데이터 저장-여행지 종료 시간
                    SpotList.put("SpotEndTime",extractTime(time));


                    //길찾기-호텔 호출
//                    DirectionsResult route = directionsService.getDirections(currentLat, currentLng, BestHotel.geometry.location.lat, BestHotel.geometry.location.lng);
                    DirectionsResult route = directionsService.getNameDirections(spots.get(i).getAsJsonObject().get("name").getAsString(),BestHotel.name);

                    PlaceDetails placeDetails = ggp_controller.searchPlacesDetail(BestHotel.placeId);

                    HotelData = new LinkedHashMap<>();
                    HotelData.put("Address",placeDetails.formattedAddress);
                    HotelData.put("Lat",BestHotel.geometry.location.lat);
                    HotelData.put("Lng",BestHotel.geometry.location.lng);


                    // 총 이동 시간을 담을 변수 (초 단위로 저장)
                    long totalDurationInSeconds = 0;

                    // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                    for (DirectionsLeg leg : route.routes[0].legs) {
                        // 각 leg에서 duration을 가져와 총 시간에 더함
                        Duration duration = leg.duration;
                        totalDurationInSeconds += duration.inSeconds;
                    }

                    // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                    long totalDurationInMinutes = totalDurationInSeconds / 60;

                    String SpotTotaltips =
                            String.valueOf((spot.getAsJsonObject("stats").get("total_tips").getAsInt()
                                    +spot.getAsJsonObject("stats").get("total_ratings").getAsInt()
                                    +spot.getAsJsonObject("stats").get("total_photos").getAsInt()));

                    SpotList.put("SpotName",spot.get("name").getAsString());
                    SpotList.put("SpotLat",currentLat);
                    SpotList.put("SpotLng",currentLng);
                    SpotList.put("SpotTotaltips",SpotTotaltips);
                    SpotList.put("SpotRating",spot.get("rating").getAsString());
                    SpotList.put("SpotDescription",spot.get("PlaceDescription").getAsString());
                    SpotList.put("SpotPhoto",spot.get("photourl").getAsString());
                    SpotList.put("DirectionTime",totalDurationInMinutes);
                    SpotList.put("DirectionURL","https://www.google.com/maps/dir/?api=1&origin="+currentLat+","+currentLng+"&destination="+BestHotel.geometry.location.lat+","+BestHotel.geometry.location.lng+"&travelmode=transit");
                    SpotList.put("searchPlacesDetail","https://www.google.com/maps/search/"+spot.get("name").getAsString());

//                    time = time.plusMinutes(totalDurationInMinutes);
                }


                TotalSpotList.add(SpotList);
                Plan.put("HotelData",HotelData);
            }

            Plan.put("TotalSpotList",TotalSpotList);
            Plan.put("DateEndTime",extractTime(time));


            travelPlans.add(Plan);
        }


        System.out.println("종료");
        return travelPlans;
    }



    @PostMapping("/TravelPlanMaking")
    public ResponseEntity<List<Map<String, Object>>> TravelPlanMaking(@RequestBody TravelPlanRequest request) throws Exception {
        System.out.println("Received Data: " + request);

        System.out.println("Location: " + request.getLocation());
        System.out.println("Start Date: " + request.getStartDate());
        System.out.println("End Date: " + request.getEndDate());
        System.out.println("Time Ranges: " + request.getTimeRanges());
        System.out.println("Selected Tags: " + Arrays.toString(request.getSelectedTags()));
        System.out.println("Transportation: " + request.getTransportation());


        List<Map<String, Object>> travelPlans = MakePlan(request);

        return ResponseEntity.ok(travelPlans);
    }




}

