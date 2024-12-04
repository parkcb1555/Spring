package com.sw.springboot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.model.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

//import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.temporal.ChronoUnit;

import com.sw.springboot.ChooseTravelSpot.*;
import com.sw.springboot.GooGlePlaceAPI.*;
import com.sw.springboot.WeatherAPI.weather;
import com.sw.springboot.WeatherAPI.WeatherService;


@Controller
public class TravelSetUp {
    @Autowired
    RecommendationScore recommendationScore;

    @Autowired
    GGP_Service ggp_service;

    @Autowired
    DirectionsService directionsService;

    @Autowired
    WeatherService weatherService;

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

    @GetMapping("/TravelSetting")
    public String TravelSetting(){
        System.out.println("TravelSetting 접근");
        return "TravelSetting";
    }

    @PostMapping("/TravelDateTimeSetting")
    public String TravelDateTimeSetting(HttpSession session, Model model, Person person) {
        System.out.println("TravelDateTimeSetting 접근");
        System.out.println(person.getCity()+"/"+person.getStartdate()+"/"+person.getEnddate()+"/"+person.getPeoplecount());

        model.addAttribute("person", person);

        session.setAttribute("person", person);

        return "TravelDateTimeSetting";  // templates 아래의 index.html을 찾음.
    }

    @PostMapping("/TravelTagSetView")
    public String TravelTagSetView(HttpServletRequest request,HttpSession session ,Model model){
        // 세션에서 person 객체를 가져와 모델에 추가

        Person person = (Person) session.getAttribute("person");
        System.out.println("TravelTagSetView 접근");
        model.addAttribute("person", person);
        System.out.println("person>>"+person.getCity()+"/"+person.getStartdate()+"/"+person.getEnddate()+"/"+person.getPeoplecount());


        // "start-date"와 "end-date" 값 가져오기
        String startDate = request.getParameter("start-date");
        String endDate = request.getParameter("end-date");

        // 날짜와 시간 데이터를 저장할 Map 생성
        Map<String, String[]> dateTimeMap = new LinkedHashMap<>();

        // 시작 날짜와 종료 날짜 범위 확인
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        while (!start.isAfter(end)) {
            String dateString = start.toString();
            String startTime = request.getParameter("appt-start-" + dateString);
            String endTime = request.getParameter("appt-end-" + dateString);

            // 날짜와 시작, 종료 시간을 배열로 저장
            dateTimeMap.put(dateString, new String[] {startTime, endTime});

            // 다음 날짜로 증가
            start = start.plusDays(1);
        }

        // Model에 날짜와 시간 정보 추가
        model.addAttribute("dateTimeMap", dateTimeMap);

        return "TravelTagSetView";
    }


    @PostMapping("/ShowPlan")
    public String ShowPlan(@RequestParam Map<String, String> parameters,@RequestParam("tags[]") String[] tags,HttpServletRequest request,HttpSession session ,Model model) throws Exception {
        System.out.println("ShowPlan 접근");
        Person person = (Person) session.getAttribute("person");
        System.out.println("person>>"+person.getCity()+"/"+person.getStartdate()+"/"+person.getEnddate()+"/"+person.getPeoplecount());


        // tag 값 받기
        String tag = parameters.get("tag");
        for (String ta : tags) {
            System.out.println("선택된 태그: " + ta);
        }
        // 그 외의 값 처리
        String city = parameters.get("city");
        String startdate = parameters.get("startdate");
        String enddate = parameters.get("enddate");

        String peoplecount = parameters.get("peoplecount");


        // 시작 날짜와 종료 날짜 범위 확인
        LocalDate start = LocalDate.parse(startdate);
        LocalDate end = LocalDate.parse(enddate);

        // 날짜와 시간 데이터를 저장할 Map 생성
        Map<String, String[]> dateTimeMap = new LinkedHashMap<>();

        // 날짜별로 데이터를 받아오기
        while (!start.isAfter(end)) {
            String dateString = start.toString();

            // request에서 starttime, endtime 받아오기
            String startTime = request.getParameter("starttime_" + dateString);
            String endTime = request.getParameter("endtime_" + dateString);

            System.out.println("start="+start+" startTime="+startTime+" endTime="+endTime);

            // 날짜와 시작, 종료 시간을 배열로 저장
            dateTimeMap.put(dateString, new String[] { startTime, endTime });

            // 다음 날짜로 증가
            start = start.plusDays(1);
        }


        // 모델에 데이터 추가
        model.addAttribute("city", city);
        model.addAttribute("startdate", startdate);
        model.addAttribute("enddate", enddate);
        model.addAttribute("peoplecount", peoplecount);
        model.addAttribute("tag", tag);

        weather weather = new weather();
        Map<LocalDate, Double> VilageWeatherData = Map.of();
        String MidWeather="";

        //기상청 api 단기예보 호출
        VilageWeatherData = weather.VilageFcstInfoService(weatherService.getWeatherApiKey(),LocalDate.parse(startdate),LocalDate.parse(enddate));

        if(LocalDate.parse(enddate).isAfter(LocalDate.now().plusDays(2))) {
            //기상청 api 중기예보 호출
            MidWeather = weather.MidFcstInfoService(weatherService.getWeatherApiKey(),start,end);
        }

        Map<LocalDateTime, JsonArray> result = recommendationScore.ChooseTravelSpot("서울",tags,startdate,enddate,dateTimeMap,directionsService.getKey());

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


        // DirectionsService 호출을 위한 좌표 추출 예제
        for (Map.Entry<LocalDateTime, JsonArray> entry : result.entrySet()) {
            List<Map<String, String>> spotDetails = new ArrayList<>();
            LocalDateTime time = entry.getKey();
            LocalDate nowdate = time.toLocalDate();
            JsonArray spots = entry.getValue();  // entry에서 spots 배열 추출
            Boolean lunchtime = false;
            Boolean dinertime = false;


            if(LocalDate.parse(enddate).isAfter(LocalDate.now().plusDays(2))) {
                String nowWeather = MidWeatherSet(LocalDate.now(),nowdate,MidWeather);
                model.addAttribute("nowWeather", nowWeather);
            }else{
                String nowWeather = VilageWeatherSet(nowdate, VilageWeatherData);
                model.addAttribute("nowWeather", nowWeather);
            }

            // spots 배열에서 순차적으로 각 spot 처리
            for (int i = 0; i < spots.size(); i++) {
                JsonObject spot = spots.get(i).getAsJsonObject();  // 각 spot을 JsonObject로 변환
                Map<String, String> spotInfo = new HashMap<>();
                long RegularTime = 0;
                int nowtime = time.getHour();
                int Endhour = 0;

                System.out.println("time="+time);
                System.out.println("nowtime="+nowtime);

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

                    PlacesSearchResult firstPlace = PlacesSearchresponse.results[0];

                    //길찾기-호텔 호출
                    DirectionsResult route = directionsService.getDirections(currentLat, currentLng, firstPlace.geometry.location.lat, firstPlace.geometry.location.lng);

                    PlaceDetails placeDetails = ggp_controller.searchPlacesDetail(firstPlace.placeId);

                    String photoUrl = "https://maps.googleapis.com/maps/api/place/photo"
                            + "?maxwidth=400"
                            + "&photo_reference=" + placeDetails.photos[0].photoReference
                            + "&key="+ggp_service.printApiKey();

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

                    model.addAttribute("hotelphotourl", photoUrl);
                    model.addAttribute("hotelname", placeDetails.name);
                    model.addAttribute("hotelvicinity", placeDetails.vicinity);
                    model.addAttribute("hotelrating", placeDetails.rating);
                    model.addAttribute("hoteltotal_tips", placeDetails.userRatingsTotal);
                    model.addAttribute("hotelPlaceDescription", placeDetails.editorialSummary.overview);

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

                        JsonObject Restaurant = recommendationScore.ChooseRestaurant(currentLat,currentLng,directionsService.getKey());
                        double RestaurantLat = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("latitude").getAsDouble();
                        double RestaurantLng = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("longitude").getAsDouble();

                        DirectionsResult route = directionsService.getDirections(currentLat, currentLng, RestaurantLat, RestaurantLng);

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

                        JsonObject Restaurant = recommendationScore.ChooseRestaurant(currentLat,currentLng,directionsService.getKey());
                        double RestaurantLat = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("latitude").getAsDouble();
                        double RestaurantLng = Restaurant.getAsJsonObject("geocodes").getAsJsonObject("main").get("longitude").getAsDouble();

                        DirectionsResult route = directionsService.getDirections(currentLat, currentLng, RestaurantLat, RestaurantLng);

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
                            // 각 leg에서 duration을 가져와 총 시간에 더함
                            Duration duration = leg.duration;
                            totalDurationInSeconds += duration.inSeconds;
                        }

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

                    PlacesSearchResult firstPlace = PlacesSearchresponse.results[0];

                    spotInfo.put("StartTime", String.valueOf(time));
                    time = time.plusMinutes(RegularTime);
                    spotInfo.put("EndTime", String.valueOf(time));
                    spotDetails.add(spotInfo);
                    
                    //길찾기-호텔 호출
                    DirectionsResult route = directionsService.getDirections(currentLat, currentLng, firstPlace.geometry.location.lat, firstPlace.geometry.location.lng);

                    PlaceDetails placeDetails = ggp_controller.searchPlacesDetail(firstPlace.placeId);

                    String photoUrl = "https://maps.googleapis.com/maps/api/place/photo"
                            + "?maxwidth=400"
                            + "&photo_reference=" + placeDetails.photos[0].photoReference
                            + "&key="+ggp_service.printApiKey();




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

                    model.addAttribute("hotelphotourl", photoUrl);
                    model.addAttribute("hotelname", placeDetails.name);
                    model.addAttribute("hotelvicinity", placeDetails.vicinity);
                    model.addAttribute("hotelrating", placeDetails.rating);
                    model.addAttribute("hoteltotal_tips", placeDetails.userRatingsTotal);
                    model.addAttribute("hotelPlaceDescription", placeDetails.editorialSummary.overview);

                }
            }

            // 처리된 spotDetails를 processedResult에 넣기
            processedResult.put(entry.getKey(), spotDetails);
        }


        model.addAttribute("Spotresult", processedResult);

        System.out.println("종료");
        return "ShowPlan";
    }

}


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Person {
    private String city;
    private String startdate;
    private String enddate;
    private int peoplecount;

    // Getter, Setter, 생성자 등
}

