package com.sw.springboot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public String MidWeatherSet(LocalDate localdatenow,LocalDate nowdate,String MidWeatherResult){
        String result = "";
        long daysBetween = ChronoUnit.DAYS.between(localdatenow, nowdate);
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

            for (String key : keys) {
                if (item.has(key)) {
                    if(daysBetween==3){
                        result = item.get("wf3Am").getAsString();
                        System.out.println(result);
                    }else if(daysBetween==4){
                        result = item.get("wf4Am").getAsString();
                        System.out.println(result);
                    }else if(daysBetween==5){
                        result = item.get("wf5Am").getAsString();
                        System.out.println(result);
                    }else if(daysBetween==6){
                        result = item.get("wf6Am").getAsString();
                    }else if(daysBetween==7){
                        result = item.get("wf7Am").getAsString();
                    }else if(daysBetween==8){
                        result = item.get("wf8").getAsString();
                    }else if(daysBetween==9){
                        result = item.get("wf9").getAsString();
                    }else if(daysBetween==10){
                        result = item.get("wf10").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public List<List<Object>> MakePlan(TravelPlanRequest request) throws Exception {
        //최종적으로 클라이언트로 전달할 리스트
        List<List<Object>> travelPlans = new ArrayList<>();

        System.out.println("MakePlan 접근");
        System.out.println("Location: " + request.getLocation());
        System.out.println("Start Date: " + request.getStartDate());
        System.out.println("End Date: " + request.getEndDate());
        System.out.println("Time Ranges: " + request.getTimeRanges());
        System.out.println("Selected Tags: " + Arrays.toString(request.getSelectedTags()));
        System.out.println("Transportation: " + request.getTransportation());

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

                System.out.println("firstday-"+firstday+" Time Range - Start: " + start + ", End: " + end);

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

        if(LocalDate.parse(enddate).isAfter(LocalDate.now().plusDays(2))) {
            //기상청 api 중기예보 호출
            MidWeather = weather.MidFcstInfoService(weatherService.printApiKey(),start,end);
        }

        Map<LocalDateTime, JsonArray> result = recommendationScore.ChooseTravelSpot(request.getSelectedTags(),startdate,enddate,dateTimeMap);

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
            //여행 날짜/작성한 여행 장소들/다음장소 길찾기/날씨/숙소 저장할 리스트
            List<Object> Plan = new ArrayList<>();

            List<Map<String, String>> spotDetails = new ArrayList<>();
            LocalDateTime time = entry.getKey();
            LocalDate nowdate = time.toLocalDate();
            JsonArray spots = entry.getValue();  // entry에서 spots 배열 추출
            Boolean lunchtime = false;
            Boolean dinertime = false;

            //날짜 데이터 삽입
            Plan.add(nowdate);
            System.out.println("nowdate>>"+nowdate+"   LocalDate.parse(enddate)>>>"+LocalDate.parse(enddate));
            
            if(LocalDate.parse(enddate).isAfter(LocalDate.now().plusDays(2))) {
                String nowWeather = MidWeatherSet(LocalDate.now(),nowdate,MidWeather);
                System.out.println(nowWeather);
                //날씨 데이터 삽입
                Plan.add(nowWeather);
            }else{
                String nowWeather = VilageWeatherSet(nowdate, VilageWeatherData);
                System.out.println(nowWeather);
                //날씨 데이터 삽입
                Plan.add(nowWeather);
            }


            //여행 장소 리스트
            JsonArray Spotlist = new JsonArray();
            //여행 장소 길찾기 리스트
            JsonArray Directionlist = new JsonArray();

            // spots 배열에서 순차적으로 각 spot 처리
            for (int i = 0; i < spots.size(); i++) {
                JsonObject spot = spots.get(i).getAsJsonObject();  // 각 spot을 JsonObject로 변환
                Map<String, String> spotInfo = new HashMap<>();
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
                        System.out.println("LocalDate.parse(date).equals(nowdate)!!! date-"+date+">>>nowdate-"+nowdate);
                        // getHour() 메서드로 시간 추출
                        Endhour = hourendtime.getHour();

                        System.out.println(startTime+">>"+endTime+">>"+Endhour);
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
                    DirectionsResult route = directionsService.getDirections(currentLat, currentLng, BestHotel.geometry.location.lat, BestHotel.geometry.location.lng);


                    Spotlist.add(spots.get(i-1).getAsJsonObject());

                    Directionlist.add(route.toString());

                    Plan.add(Spotlist);
                    Plan.add(Directionlist);
                    Plan.add(BestHotel);


                    PlaceDetails placeDetails = ggp_controller.searchPlacesDetail(BestHotel.placeId);

                    String photoUrl = "https://maps.googleapis.com/maps/api/place/photo"
                            + "?maxwidth=400"
                            + "&photo_reference=" + placeDetails.photos[0].photoReference
                            + "&key="+ggp_service.printApiKey();

                    Plan.add(photoUrl);

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
                    spotInfo.put("totalDurationInMinutes", String.valueOf(totalDurationInMinutes));
//                    spotInfo.put("EndTime", String.valueOf(time));


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
                    spotInfo.put("name", spot.get("name").getAsString());
                    spotInfo.put("rating", spot.has("rating") ?spot.get("rating").getAsString() : "값없음");
                    spotInfo.put("address", spot.getAsJsonObject("location").get("address").getAsString());
                    JsonObject stats = spot.getAsJsonObject("stats");  // stats 객체 추출
                    spotInfo.put("ratingsCount", stats.has("total_ratings") ? stats.get("total_ratings").getAsString() : "값 없음");
                    spotInfo.put("total_tips", stats.has("total_tips") ? stats.get("total_tips").getAsString() : "값 없음");
                    spotInfo.put("PlaceDescription", spot.has("PlaceDescription") ? spot.get("PlaceDescription").getAsString() : "값 없음");
                    spotInfo.put("photourl", spot.has("photourl") ? spot.get("photourl").getAsString() : "값 없음");

                    spotInfo.put("StartTime", String.valueOf(time));
                    time = time.plusMinutes(RegularTime);
                    spotInfo.put("EndTime", String.valueOf(time));

                    //오후 12시 이상이 되어 점심 시간이 된 경우
                    if (time.getHour() >= 12 && !lunchtime){
                        System.out.println("점심시간!!!!!!!!!!");
                        System.out.println(time.getHour());

                        JsonObject Restaurant = recommendationScore.ChooseRestaurant(currentLat,currentLng);
                        double RestaurantLat = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("latitude").getAsDouble();
                        double RestaurantLng = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("longitude").getAsDouble();

                        DirectionsResult route = directionsService.getDirections(currentLat, currentLng, RestaurantLat, RestaurantLng);

                        // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                        for (DirectionsLeg leg : route.routes[0].legs) {
                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }


                        Spotlist.add(spots.get(i).getAsJsonObject());

                        Directionlist.add(route.toString());


                        // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                        long totalDurationInMinutes = totalDurationInSeconds / 60;
                        time = time.plusMinutes(totalDurationInMinutes);
                        spotInfo.put("totalDurationInMinutes", String.valueOf(totalDurationInMinutes));

                        //현재 고른 장소 -> 점심 식사 장소 저장
                        spotDetails.add(spotInfo);

                        //점심 식사 장소 -> 다음 장소 저장
                        spotInfo = new HashMap<>();

                        route = directionsService.getDirections(RestaurantLat, RestaurantLng,nextLat,nextLng);

                        // 총 이동 시간을 담을 변수 (초 단위로 저장)
                        totalDurationInSeconds = 0;

                        // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                        for (DirectionsLeg leg : route.routes[0].legs) {
                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }


                        // 필요한 정보를 spotInfo에 넣기
                        spotInfo.put("name", Restaurant.get("name").getAsString());
                        spotInfo.put("address", Restaurant.getAsJsonObject("location").get("address").getAsString());
                        RegularTime = Long.parseLong(Restaurant.get("RegularTime").getAsString());

                        stats = Restaurant.getAsJsonObject("stats");  // stats 객체 추출
                        spotInfo.put("rating", Restaurant.has("rating") ? Restaurant.get("rating").getAsString() : "값 없음");
                        spotInfo.put("ratingsCount", stats.has("total_ratings") ? stats.get("total_ratings").getAsString() : "값 없음");
                        spotInfo.put("total_tips", stats.has("total_tips") ? stats.get("total_tips").getAsString() : "값 없음");
                        spotInfo.put("PlaceDescription", Restaurant.has("PlaceDescription") ? Restaurant.get("PlaceDescription").getAsString() : "값 없음");
                        spotInfo.put("photourl", Restaurant.has("photourl") ? Restaurant.get("photourl").getAsString() : "값 없음");

                        spotInfo.put("StartTime", String.valueOf(time));
                        time = time.plusMinutes(RegularTime);
                        spotInfo.put("EndTime", String.valueOf(time));


                        Spotlist.add(spots.get(i + 1).getAsJsonObject());
                        Directionlist.add(route.toString());


                        // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                        totalDurationInMinutes = totalDurationInSeconds / 60;
                        time = time.plusMinutes(totalDurationInMinutes);
                        spotInfo.put("totalDurationInMinutes", String.valueOf(totalDurationInMinutes));

                        System.out.println("점심식사 이후 시간>>"+time);

                        //현재 고른 장소 -> 점심 식사 장소 저장
                        spotDetails.add(spotInfo);
                        lunchtime = true;

                        System.out.println("점심시간 종료!!!!!!!!");
                    }
                    //18시 이상이 되어 저녁 시간이 된 경우
                    else if (nowtime >= 18 && !dinertime) {
                        System.out.println("저녁시간!!!!!!!!!!");
                        System.out.println(time.getHour());

                        JsonObject Restaurant = recommendationScore.ChooseRestaurant(currentLat,currentLng);
                        double RestaurantLat = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("latitude").getAsDouble();
                        double RestaurantLng = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("longitude").getAsDouble();

                        DirectionsResult route = directionsService.getDirections(currentLat, currentLng, RestaurantLat, RestaurantLng);

                        // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                        for (DirectionsLeg leg : route.routes[0].legs) {
                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }


                        Spotlist.add(spots.get(i).getAsJsonObject());
                        Directionlist.add(route.toString());


                        // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                        long totalDurationInMinutes = totalDurationInSeconds / 60;
                        time = time.plusMinutes(totalDurationInMinutes);
                        spotInfo.put("totalDurationInMinutes", String.valueOf(totalDurationInMinutes));

                        //현재 고른 장소 -> 저녁 식사 장소 저장
                        spotDetails.add(spotInfo);

                        //저녁 식사 장소 -> 다음 장소 저장용
                        spotInfo = new HashMap<>();

                        route = directionsService.getDirections(RestaurantLat, RestaurantLng,nextLat,nextLng);

                        // 총 이동 시간을 담을 변수 (초 단위로 저장)
                        totalDurationInSeconds = 0;

                        // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                        for (DirectionsLeg leg : route.routes[0].legs) {
                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }


                        // 필요한 정보를 spotInfo에 넣기
                        spotInfo.put("name", Restaurant.get("name").getAsString());
                        spotInfo.put("address", Restaurant.getAsJsonObject("location").get("address").getAsString());
                        RegularTime = Long.parseLong(Restaurant.get("RegularTime").getAsString());

                        stats = Restaurant.getAsJsonObject("stats");  // stats 객체 추출
                        spotInfo.put("rating", Restaurant.has("rating") ? Restaurant.get("rating").getAsString() : "값 없음");
                        spotInfo.put("ratingsCount", stats.has("total_ratings") ? stats.get("total_ratings").getAsString() : "값 없음");
                        spotInfo.put("total_tips", stats.has("total_tips") ? stats.get("total_tips").getAsString() : "값 없음");
                        spotInfo.put("PlaceDescription", Restaurant.has("PlaceDescription") ? Restaurant.get("PlaceDescription").getAsString() : "값 없음");
                        spotInfo.put("photourl", Restaurant.has("photourl") ? Restaurant.get("photourl").getAsString() : "값 없음");

                        spotInfo.put("StartTime", String.valueOf(time));
                        time = time.plusMinutes(RegularTime);
                        spotInfo.put("EndTime", String.valueOf(time));


                        Spotlist.add(spots.get(i + 1).getAsJsonObject());
                        Directionlist.add(route.toString());


                        // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                        totalDurationInMinutes = totalDurationInSeconds / 60;
                        time = time.plusMinutes(totalDurationInMinutes);
                        spotInfo.put("totalDurationInMinutes", String.valueOf(totalDurationInMinutes));

                        System.out.println("저녁식사 이후 시간>>"+time);

                        //저녁 식사 장소 -> 다음 장소 저장
                        spotDetails.add(spotInfo);
                        dinertime = true;

                        System.out.println("저녁 종료!!!!!!!!");
                    }
                    else{
                        DirectionsResult route = directionsService.getDirections(currentLat, currentLng, nextLat, nextLng);
                        // DirectionsResult에서 routes를 가져와 첫 번째 route를 사용
                        for (DirectionsLeg leg : route.routes[0].legs) {

                            System.out.println();
                            System.out.println();
                            System.out.println(Arrays.toString(route.routes));

                            System.out.println();
                            System.out.println();
                            System.out.println(leg);


                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }

                        Spotlist.add(spots.get(i).getAsJsonObject());
                        Directionlist.add(route.toString());

                        // 총 이동 시간을 분 단위로 계산 (초를 60으로 나눔)
                        long totalDurationInMinutes = totalDurationInSeconds / 60;
                        time = time.plusMinutes(totalDurationInMinutes);
                        spotInfo.put("totalDurationInMinutes", String.valueOf(totalDurationInMinutes));

                        //현재 고른 장소 -> 점심 식사 장소 저장
                        spotDetails.add(spotInfo);
                    }

                }
                else{
                    //일자별로 가장 마지막 장소
                    spotInfo.put("name", spot.get("name").getAsString());

                    RegularTime = Long.parseLong(spot.get("RegularTime").getAsString());

                    spotInfo.put("rating", spot.has("rating") ? spot.get("rating").getAsString() : "값 없음");
                    spotInfo.put("address", spot.getAsJsonObject("location").get("address").getAsString());
                    JsonObject stats = spot.getAsJsonObject("stats");  // stats 객체 추출
                    spotInfo.put("ratingsCount", stats.has("total_ratings") ? stats.get("total_ratings").getAsString() : "값 없음");
                    spotInfo.put("total_tips", stats.has("total_tips") ? stats.get("total_tips").getAsString() : "값 없음");
                    spotInfo.put("PlaceDescription", spot.has("PlaceDescription") ? spot.get("PlaceDescription").getAsString() : "값 없음");
                    spotInfo.put("photourl", spot.has("photourl") ? spot.get("photourl").getAsString() : "값 없음");

                    JsonObject currentSpot = spots.get(i).getAsJsonObject().getAsJsonObject("geocodes").getAsJsonObject("main");
                    double currentLat = currentSpot.get("latitude").getAsDouble();
                    double currentLng = currentSpot.get("longitude").getAsDouble();

                    PlacesSearchResult BestHotel = PlacesSearchresponse.results[0];

                    spotInfo.put("StartTime", String.valueOf(time));
                    time = time.plusMinutes(RegularTime);
                    spotInfo.put("EndTime", String.valueOf(time));
                    spotDetails.add(spotInfo);

                    //길찾기-호텔 호출
                    DirectionsResult route = directionsService.getDirections(currentLat, currentLng, BestHotel.geometry.location.lat, BestHotel.geometry.location.lng);

                    PlaceDetails placeDetails = ggp_controller.searchPlacesDetail(BestHotel.placeId);

                    Spotlist.add(spots.get(i).getAsJsonObject());
                    Directionlist.add(route.toString());

                    Plan.add(Spotlist);
                    Plan.add(Directionlist);
                    Plan.add(BestHotel);

                    String photoUrl = "https://maps.googleapis.com/maps/api/place/photo"
                            + "?maxwidth=400"
                            + "&photo_reference=" + placeDetails.photos[0].photoReference
                            + "&key="+ggp_service.printApiKey();

                    Plan.add(photoUrl);


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
                    time = time.plusMinutes(totalDurationInMinutes);
                    spotInfo.put("totalDurationInMinutes", String.valueOf(totalDurationInMinutes));
//                    spotInfo.put("EndTime", String.valueOf(time));


                }
            }

            travelPlans.add(Plan);

            // 처리된 spotDetails를 processedResult에 넣기
            processedResult.put(entry.getKey(), spotDetails);
        }

        System.out.println(travelPlans);

        System.out.println("종료");
        return travelPlans;
    }



    @PostMapping("/TravelPlanMaking")
    public void TravelPlanMaking(@RequestBody TravelPlanRequest request) throws Exception {
        System.out.println("Received Data: " + request);

        System.out.println("Location: " + request.getLocation());
        System.out.println("Start Date: " + request.getStartDate());
        System.out.println("End Date: " + request.getEndDate());
        System.out.println("Time Ranges: " + request.getTimeRanges());

        List<Map<String, String>> timeRanges = request.getTimeRanges();
        if (timeRanges != null) {
            for (Map<String, String> timeRange : timeRanges) {
                String start = timeRange.get("start"); // start 값 추출
                String end = timeRange.get("end");    // end 값 추출
                System.out.println("Time Range - Start: " + start + ", End: " + end);
            }
        }

        System.out.println("Selected Tags: " + Arrays.toString(request.getSelectedTags()));
        System.out.println("Transportation: " + request.getTransportation());



        List<List<Object>> travelPlans = MakePlan(request);

        for (List<Object> plan : travelPlans) {
            System.out.println(plan);
        }

    }




}

