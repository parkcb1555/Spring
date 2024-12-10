package com.sw.springboot.ChooseTravelSpot;


import com.sw.springboot.FoursquareAPI.FoursquareService;
import com.sw.springboot.FoursquareAPI.foursquareRequest;
import com.sw.springboot.GptAPI.GPT_API;
import com.sw.springboot.GptAPI.GPT_API_Compent;
import com.sw.springboot.WeatherAPI.*;
import com.sw.springboot.GooGlePlaceAPI.DirectionsController;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import java.time.*;
import java.util.*;
import java.math.*;
import java.time.format.*;
import java.time.Duration.*;


@RestController
public class RecommendationScore {
    @Value("${foursquare.api.key}")
    private String foursquareApiKey;

    @Autowired
    FoursquareService foursquareService;


    @Autowired
    GPT_API_Compent gptApiCompent;


    @Autowired
    DirectionsController directionsController;

    //여행지 점수
    @Getter
    public class ChooseSpotScore{

        //인기점수
        @Setter
        int PopularityScore;

        //관람점수
        @Setter
        int WatchedScore;

        //태그점수
        @Setter
        int TagScore;

        //여행지 태그
        @Setter
        String SpotTag;

        //근처 관광지 점수
        @Setter
        int NearSpotScore;

        //시간점수
        @Setter
        int RegularHoursScore;

        //등수
        @Setter
        int Ranking;

        //이름
        @Setter
        String SpotName;

        //관람시간(분단위)
        @Setter
        int RegularTime;

        // 총점 필드
        @Setter
        private int totalScore;

    }

    @Autowired
    foursquareRequest foursquareRequest;


    //시간 점수 구하기
    public int RegularHoursScoreSet(JsonArray TravelSpotArray, String SpotName, LocalTime NowTime,int currentDayOfWeek,int RegularTime){
        int score = 0;
        int openTotalMinutes = 0;
        int closeTotalMinutes=0;
        int openHour = 0;
        int closeHour = 0;
        for (int i = 0; i < TravelSpotArray.size(); i++) {
            JsonObject place = TravelSpotArray.get(i).getAsJsonObject();

            if(place.get("name").getAsString().equals(SpotName)){
                JsonObject hours = place.getAsJsonObject("hours");
                JsonArray regularArray = hours.getAsJsonArray("regular");
                for (int j = 0; j < regularArray.size(); j++) {
                    JsonObject dayInfo = regularArray.get(j).getAsJsonObject();
                    int day = dayInfo.get("day").getAsInt();


                    if (day == currentDayOfWeek) {
                        // open과 close를 int로 변환
                        int openTime = Integer.parseInt(dayInfo.get("open").getAsString());
                        int closeTime = Integer.parseInt(dayInfo.get("close").getAsString());

                        // 운영 시간 계산 (분 단위로 계산)
                        openHour = openTime / 100;  // 시
                        int openMinute = openTime % 100;  // 분
                        closeHour = closeTime / 100;  // 시
                        int closeMinute = closeTime % 100;  // 분

                        // 각각 시와 분을 분 단위로 환산하여 계산
                        openTotalMinutes = openHour * 60 + openMinute;
                        closeTotalMinutes = closeHour * 60 + closeMinute;

                        break;
                    }
                }
                if (openTotalMinutes == 0 && closeTotalMinutes == 0) {
                    System.out.println("운영 시간이 설정되지 않았습니다.");
                    break;
                }else{
                    // 운영 시간(분) 구하기
                    int totalOperatingMinutes = (closeTotalMinutes - openTotalMinutes);
                    totalOperatingMinutes -= RegularTime;
                    int Operatinghours = totalOperatingMinutes / 60;
                    double Operatingminutes = (totalOperatingMinutes % 60) / 60.0; // 분을 소수점 값으로 변환

                    // 시와 분을 합쳐서 점수 계산
                    double totalTimeInHours = Operatinghours + Operatingminutes;

                    // 운영 시간 여부 확인 및 점수 설정
                    if (NowTime.getHour() < openHour || NowTime.getHour() > closeHour) {
                        score = 0;
                    } else {
                        //리턴할 시간 점수 =  100 / (총 운영시간)
                        score = (int) (100 / totalTimeInHours);
                    }
                }
            }else{
                continue;
            }

        }

        return score;
    }



    //ScoreListByTime에서 총합 점수가 가장 높은 ArrayList<ChooseSpotScore>를 찾는 함수
    public Map.Entry<LocalTime, ArrayList<ChooseSpotScore>> findHighestScoreList(
        Map<LocalTime, ArrayList<ChooseSpotScore>> scoreListByTime) {
        Map.Entry<LocalTime, ArrayList<ChooseSpotScore>> highestScoreEntry = null;
        int maxTotalScore = Integer.MIN_VALUE;

        for (Map.Entry<LocalTime, ArrayList<ChooseSpotScore>> entry : scoreListByTime.entrySet()) {
            int totalScore = calculateTotalScore(entry.getValue());
            if (totalScore > maxTotalScore) {
                maxTotalScore = totalScore;
                highestScoreEntry = entry;
            }
        }

        return highestScoreEntry;
    }

    //ArrayList<ChooseSpotScore>의 총합 점수를 계산하는 함수
    public int calculateTotalScore(ArrayList<ChooseSpotScore> scores) {
        int total = 0;
        for (ChooseSpotScore score : scores) {
            total += score.getTotalScore();
        }
        return total;
    }

    // 관광지 기본 점수 계산(인기점수+관람점수+근처 여행지 점수)
    public int calculateTotalScore(ChooseSpotScore score) {
        return score.PopularityScore + score.WatchedScore  + score.NearSpotScore;
    }

    //근처 관광지 개수 확인
    public int NearSpotCounting(JsonArray TravelSpotArray,JsonObject NowPlace){
        int NearSpotCount = 0;
        for (int i = 0; i < TravelSpotArray.size(); i++) {
            JsonObject place = TravelSpotArray.get(i).getAsJsonObject();
            if (!NowPlace.get("name").getAsString().equals(place.get("name").getAsString())){
                double latitude1 = NowPlace.getAsJsonObject("geocodes").getAsJsonObject("main").get("latitude").getAsDouble();
                double longitude1 = NowPlace.getAsJsonObject("geocodes").getAsJsonObject("main").get("longitude").getAsDouble();

                double latitude2 = place.getAsJsonObject("geocodes").getAsJsonObject("main").get("latitude").getAsDouble();
                double longitude2 = place.getAsJsonObject("geocodes").getAsJsonObject("main").get("longitude").getAsDouble();

                double dist = distance(latitude1,longitude1,latitude2,longitude2);
                if (dist <= 3000){
                    NearSpotCount+=1;
                }
            }
        }
        return NearSpotCount;
    }

    // 두 좌표 사이의 거리를 구하는 함수
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = deg2rad(lon1 - lon2);
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(theta);
        dist = Math.acos(Math.min(1.0, Math.max(-1.0, dist))); // 값 제한
        dist = rad2deg(dist) * 60 * 1.1515 * 1609.344; // 미터 단위
        return dist;
    }

    // 10진수를 radian(라디안)으로 변환
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // radian(라디안)을 10진수로 변환
    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }



    //첫 여행지 점수 계산
    public int calculateFirstTotalScore(ChooseSpotScore score) {
        return score.totalScore + score.RegularHoursScore;
    }


    // saveSpotList에 저장된 관광지와 travelSpotArray에서 이름이 같은 관광지를 JsonArray에 저장
    public JsonArray findMatchingSpots(JsonArray travelSpotArray, List<ChooseSpotScore> saveSpotList) {
        JsonArray matchedTravelSpots = new JsonArray();

        for (ChooseSpotScore savedSpot : saveSpotList) {
            for (int i = 0; i < travelSpotArray.size(); i++) {
                JsonObject travelSpot = travelSpotArray.get(i).getAsJsonObject();
                String travelSpotName = travelSpot.get("name").getAsString();
                if (savedSpot.getSpotName().equals(travelSpotName)) {
                    matchedTravelSpots.add(travelSpot);
                    break; // 일치하는 값이 있으면 더 이상 비교하지 않고 추가 후 종료
                }
            }
        }
        return matchedTravelSpots;
    }


    //선택된 카테고리
    public String CategorySetting(String[] tags){
        StringBuilder categoryBuilder = new StringBuilder();
        for (String tag : tags) {
            // 각 tag에 대한 처리 로직을 여기에 추가// 예시로 출력
            if(tag.contains("Historic")){
                categoryBuilder.append("12099,12102,12111,16011,16011,16020,16031,");
            } else if (tag.contains("Landmarks")) {
                categoryBuilder.append("16024,16025,16026,16046,");
            }else if (tag.contains("Shopping")) {
                categoryBuilder.append("17030,17033,17036,17089,17104,17105,17109,17114,17115,17116,");
            }else if (tag.contains("Market")) {
                categoryBuilder.append("17002,17054,17144,");
            }else if (tag.contains("Resort")) {
                categoryBuilder.append("19012,19016,19018,");
            }else if (tag.contains("Nature")) {
                categoryBuilder.append("16002,16003,16005,16009,16023,16028,16030,16042,16043,16053,");
            }else if (tag.contains("Park")) {
                categoryBuilder.append("16033,16034,16035,16036,16037,16038,16039,16047,16060,");
            }else if (tag.contains("Casino")) {
                categoryBuilder.append("10003,10005,10008,10033,");
            }else if (tag.contains("Spa")) {
                categoryBuilder.append("16021,18081,");
            }else if (tag.contains("Art")) {
                categoryBuilder.append("10004,10016,10028,10030,");
            }else if (tag.contains("Entertainment")) {
                categoryBuilder.append("10001,10002,10015,10019,10022,10044,10055,10056,");
            }else if (tag.contains("Cafe")) {
                categoryBuilder.append("13033,13034,13035,13036,13063,13381,");
            }


            else if(tag.contains("문화유산")){
                categoryBuilder.append("12099,12102,12111,16011,16011,16020,16031,");
            } else if (tag.contains("랜드마크")) {
                categoryBuilder.append("16024,16025,16026,16046,");
            }else if (tag.contains("쇼핑")) {
                categoryBuilder.append("17030,17033,17036,17089,17104,17105,17109,17114,17115,17116,");
            }else if (tag.contains("전통시장")) {
                categoryBuilder.append("17002,17054,17144,");
            }else if (tag.contains("휴양지")) {
                categoryBuilder.append("19012,19016,19018,");
            }else if (tag.contains("자연")) {
                categoryBuilder.append("16002,16003,16005,16009,16023,16028,16030,16042,16043,16053,");
            }else if (tag.contains("공원")) {
                categoryBuilder.append("16033,16034,16035,16036,16037,16038,16039,16047,16060,");
            }else if (tag.contains("카지노")) {
                categoryBuilder.append("10003,10005,10008,10033,");
            }else if (tag.contains("스파")) {
                categoryBuilder.append("16021,18081,");
            }else if (tag.contains("예술")) {
                categoryBuilder.append("10004,10016,10028,10030,");
            }else if (tag.contains("테마파크")) {
                categoryBuilder.append("10001,10002,10015,10019,10022,10044,10055,10056,");
            }
        }
        if (categoryBuilder.length() > 0) {
            categoryBuilder.setLength(categoryBuilder.length() - 1);
        }
        String Categorys = categoryBuilder.toString();


        return Categorys;
    }


    //선택된 카테고리 빼고 남은 카테고리 호출
    public String RestCategorySetting(String[] tags) {
        String[] allCategories = {
                "12099,12102,12111,16011,16011,16020,16031,", // 문화유산
                "16024,16025,16026,16046,",                  // 랜드마크
                "17030,17033,17036,17089,17104,17105,17109,17114,17115,17116,", // 쇼핑
                "17002,17054,17144,",                      // 전통시장
                "19012,19016,19018,",                      // 휴양지
                "16002,16003,16005,16009,16023,16028,16030,16042,16043,16053,", // 자연
                "16033,16034,16035,16036,16037,16038,16039,16047,16060,",       // 공원
                "10003,10005,10008,10033,",               // 카지노
                "16021,18081,",                           // 스파
                "10004,10016,10028,10030,",               // 예술
                "10001,10002,10015,10019,10022,10044,10055,10056," // 테마파크
        };

        String[] tagNames = {
                "문화유산", "랜드마크", "쇼핑", "전통시장", "휴양지",
                "자연", "공원", "카지노", "스파", "예술", "테마파크"
        };

        StringBuilder categoryBuilder = new StringBuilder();

        for (int i = 0; i < tagNames.length; i++) {
            boolean isExcluded = false;
            for (String tag : tags) {
                if (tag.contains(tagNames[i])) {
                    isExcluded = true;
                    break;
                }
            }
            if (!isExcluded) {
                categoryBuilder.append(allCategories[i]);
            }
        }

        if (categoryBuilder.length() > 0) {
            categoryBuilder.setLength(categoryBuilder.length() - 1); // 마지막 콤마 제거
        }

        return categoryBuilder.toString();
    }

    @GetMapping("/ChooseTravelSpot")
    public Map<LocalDateTime, JsonArray> ChooseTravelSpot(String city,String[] tags, String startdate, String enddate, Map<String, String[]> dateTimeMap,String mapapikey) throws IOException {
        System.out.println("ChooseTravelSpot 접속");
        ArrayList<ChooseSpotScore> chooseSpotScoreList = new ArrayList<>();
        ChooseSpotScore chooseSpotScore;

        String Category = CategorySetting(tags);


        //최종적으로 return 할 Map
        Map<LocalDateTime, JsonArray> FinalChooseSpotMap = new LinkedHashMap<>();

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        //포스퀘어 api 호출
        JsonArray TravelSpotArray = foursquareRequest.req(foursquareService.printApiKey(),gptApiCompent.printApiKey(),Category,city);

//===================================================================================================================
        //여행지 기본 점수 설정(인기 점수, 관람 점수,근처 관광지 점수)
        for (int i = 0; i < TravelSpotArray.size(); i++) {
            boolean placecheck;
            JsonObject place = TravelSpotArray.get(i).getAsJsonObject();
            try {
                placecheck = directionsController.hasPlaceID(mapapikey,place.get("name").getAsString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            if (!placecheck) {
                System.out.println("Skipping place: " + place.get("name").getAsString() + " (Place ID not found)");
                continue; // 아래 코드는 실행되지 않고, 다음 반복으로 넘어감
            }

            chooseSpotScore = new ChooseSpotScore();
            chooseSpotScore.setSpotName(place.get("name").getAsString());
            chooseSpotScore.setRegularTime(place.get("RegularTime").getAsInt());

            //인기 점수 설정(등수순)
            chooseSpotScore.setPopularityScore(100-i);

            //관람 점수 설정(관광지 평균 관람시간(분단위) * 4)
            chooseSpotScore.setWatchedScore(chooseSpotScore.getRegularTime() * 4);

            //3km 이내 관광지 갯수
            int NearSpotCount = NearSpotCounting(TravelSpotArray,place);
            //근처 관광지 점수 설정
            chooseSpotScore.setNearSpotScore(NearSpotCount * 20);

            //태그 설정
            chooseSpotScore.setSpotTag(place.get("tag").getAsString());
            System.out.println(place.get("tag").getAsString());
            //태그 점수 설정(20점)
            chooseSpotScore.setTagScore(20);

//            //시간 점수 설정
//            random1to100 = (int) (Math.random() * 100) + 1;
//            chooseSpotScore.setRegularHoursScore(random1to100);

            //총점(여행지 기본점수(인기 점수+근처 관광지 점수+관람점수)) 설정
            chooseSpotScore.setTotalScore(calculateTotalScore(chooseSpotScore));
            chooseSpotScoreList.add(chooseSpotScore);
        }
//===================================================================================================================

        Collections.sort(chooseSpotScoreList, Comparator
                .comparingInt(ChooseSpotScore::getTotalScore)
                .thenComparingInt(ChooseSpotScore::getPopularityScore)     // 인기점수
                .thenComparingInt(ChooseSpotScore::getNearSpotScore)       // 근처 관광지 점수
//                .thenComparingInt(ChooseSpotScore::getRegularHoursScore)   // 시간점수
//                        .thenComparingInt(ChooseSpotScore::getTagScore)            // 태그점수
                .thenComparingInt(ChooseSpotScore::getWatchedScore)        // 관람점수
                .reversed());

        //=========================================
        //여행지가 50개 미만면 선택했던 카테고리외의 나머지 카테고리 여행지 추가 호출
        if (TravelSpotArray.size() < 50) {
            String RestCategory = CategorySetting(tags);
            JsonArray MoreTravelSpotArray = foursquareRequest.req(foursquareService.printApiKey(),gptApiCompent.printApiKey(),RestCategory,city);
            for (int i = 0; i < MoreTravelSpotArray.size(); i++) {
                boolean placecheck;
                JsonObject place = MoreTravelSpotArray.get(i).getAsJsonObject();
                try {
                    placecheck = directionsController.hasPlaceID(mapapikey,place.get("name").getAsString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


                if (!placecheck) {
                    System.out.println("Skipping place: " + place.get("name").getAsString() + " (Place ID not found)");
                    continue; // 아래 코드는 실행되지 않고, 다음 반복으로 넘어감
                }

                chooseSpotScore = new ChooseSpotScore();
                chooseSpotScore.setSpotName(place.get("name").getAsString());
                chooseSpotScore.setRegularTime(place.get("RegularTime").getAsInt());

                //인기 점수 설정(등수순)
                chooseSpotScore.setPopularityScore(100-i);

                //관람 점수 설정(관광지 평균 관람시간(분단위) * 4)
                chooseSpotScore.setWatchedScore(chooseSpotScore.getRegularTime() * 4);

                //3km 이내 관광지 갯수
                int NearSpotCount = NearSpotCounting(TravelSpotArray,place);
                //근처 관광지 점수 설정
                chooseSpotScore.setNearSpotScore(NearSpotCount * 20);

                //태그 설정
                chooseSpotScore.setSpotTag(place.get("tag").getAsString());
                System.out.println(place.get("tag").getAsString());
                //태그 점수 설정(20점)
                chooseSpotScore.setTagScore(20);

//            //시간 점수 설정
//            random1to100 = (int) (Math.random() * 100) + 1;
//            chooseSpotScore.setRegularHoursScore(random1to100);

                //총점(여행지 기본점수(인기 점수+근처 관광지 점수+관람점수)) 설정
                chooseSpotScore.setTotalScore(calculateTotalScore(chooseSpotScore));
                chooseSpotScoreList.add(chooseSpotScore);
            }

            Collections.sort(chooseSpotScoreList, Comparator
                    .comparingInt(ChooseSpotScore::getTotalScore)
                    .thenComparingInt(ChooseSpotScore::getPopularityScore)     // 인기점수
                    .thenComparingInt(ChooseSpotScore::getNearSpotScore)       // 근처 관광지 점수
//                .thenComparingInt(ChooseSpotScore::getRegularHoursScore)   // 시간점수
//                        .thenComparingInt(ChooseSpotScore::getTagScore)            // 태그점수
                    .thenComparingInt(ChooseSpotScore::getWatchedScore)        // 관람점수
                    .reversed());
        }



        // dateTimeMap에 저장된 각 날짜별 시작 시간과 종료 시간을 LocalDate로 변경
        for (Map.Entry<String, String[]> Dateentry : dateTimeMap.entrySet()) {
            //하루 총 여행 시간
            int Datetotaltourtime = 0;

            String date = Dateentry.getKey(); // 날짜 (key)
            String[] times = Dateentry.getValue(); // 시작 시간과 종료 시간 (value)
            int HourpenaltyCount = 0;

            String MapstartTime = times[0];
            String MapendTime = times[1];
            startTime = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(MapstartTime));
            endTime = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(MapendTime));

            //최종적으로 반환할 Map에 저장할 ArrayList
            ArrayList<ChooseSpotScore> SaveSpotList = new ArrayList<>();

            //시간대별 가장 높은 점수를 가진 장소를 저장할 Map
            Map<LocalDateTime, ChooseSpotScore> hourlyScores = new LinkedHashMap<>();

//===================================================================================================================
            //시간대별 점수 설정
            //1시간 단위의 점수 저장
            Map<LocalTime, ArrayList<ChooseSpotScore>> ScoreListByTime = new LinkedHashMap<>();

            // 오늘의 요일 가져오기
            DayOfWeek dayOfWeek = LocalDate.parse(date).getDayOfWeek();
            System.out.println("요일 숫자 값: " + dayOfWeek.getValue()); // 월요일(1) ~ 일요일(7)

            //시작 시간
            LocalTime start = LocalTime.parse(MapstartTime);

            //종료 시간
            LocalTime end = LocalTime.parse(MapendTime);

            // 1시간 단위로 점수 설정
            while (!start.isAfter(end)) {
                // ChooseSpotScore 리스트 생성
                ArrayList<ChooseSpotScore> ScoresByTimeList = new ArrayList<>();

                for (ChooseSpotScore spotScore : chooseSpotScoreList) {
                    ChooseSpotScore hourlySpotScore = new ChooseSpotScore();
                    //여행지 이름
                    hourlySpotScore.setSpotName(spotScore.getSpotName());

                    //여행지 인기 점수
                    hourlySpotScore.setPopularityScore(spotScore.getPopularityScore());

                    //여행지 근처 관광지 점수
                    hourlySpotScore.setNearSpotScore(spotScore.getNearSpotScore());

                    //여행지 관람점수
                    hourlySpotScore.setWatchedScore(spotScore.getWatchedScore());

                    //여행지 관람시간
                    hourlySpotScore.setRegularTime(spotScore.getRegularTime());

                    //태그 설정
                    hourlySpotScore.setSpotTag(spotScore.getSpotTag());
                    //태그 점수 설정
                    hourlySpotScore.setTagScore(spotScore.getTagScore());

                    //시간 점수 계산
                    int HourlyScore = RegularHoursScoreSet(TravelSpotArray, spotScore.getSpotName(),start,dayOfWeek.getValue(),hourlySpotScore.getRegularTime());

                    //여행지 시간 점수 설정
                    hourlySpotScore.setRegularHoursScore(spotScore.getTotalScore());


                    //총 점수 설정(인기 점수+근처 관광지 점수+관람점수+시간 점수)
                    hourlySpotScore.setTotalScore(calculateTotalScore(hourlySpotScore)+HourlyScore);

                    // 리스트에 추가
                    ScoresByTimeList.add(hourlySpotScore);
                }

                // 시간대별 점수 맵에 추가
                ScoreListByTime.put(start, ScoresByTimeList);

                // 시간 1시간 증가
                start = start.plusHours(1);
            }

            // 총합 점수가 가장 높은 ArrayList<ChooseSpotScore> 찾기
            Map.Entry<LocalTime, ArrayList<ChooseSpotScore>> highestScoreEntry = findHighestScoreList(ScoreListByTime);
            startTime = LocalDateTime.of(LocalDate.parse(date), highestScoreEntry.getKey());
            ArrayList<ChooseSpotScore> hourlyList = highestScoreEntry.getValue();

//            //첫 여행지 선정
//            ArrayList<ChooseSpotScore> hourlyList = new ArrayList<>();
//
//            //첫 여행지 점수 설정
//            for (ChooseSpotScore spotScore : chooseSpotScoreList) {
//                ChooseSpotScore hourlySpotScore = new ChooseSpotScore();
//                hourlySpotScore.setSpotName(spotScore.getSpotName());
//                hourlySpotScore.setPopularityScore(spotScore.getPopularityScore());
//                hourlySpotScore.setTagScore(spotScore.getTagScore());
//                hourlySpotScore.setWatchedScore(spotScore.getWatchedScore());
//                hourlySpotScore.setNearSpotScore(spotScore.getNearSpotScore());
//                hourlySpotScore.setRegularHoursScore(spotScore.getRegularHoursScore());
//                hourlySpotScore.setTotalScore(spotScore.getTotalScore());
//                hourlySpotScore.setRanking(spotScore.getRanking());
//                hourlySpotScore.setRegularTime(spotScore.getRegularTime());
//
//                // Generate new random score for RegularHoursScore each hour
//                int randomHourlyScore = (int) (Math.random() * 100) + 1;
//                hourlySpotScore.setRegularHoursScore(0-(HourpenaltyCount*20));
//
//                // Calculate total score including hourly score
//                int firstTotalScore = calculateTotalScore(hourlySpotScore) + randomHourlyScore;
//                hourlySpotScore.setTotalScore(firstTotalScore);
//                hourlyList.add(hourlySpotScore);
//            }

            //선정된 여행지 리스트 정렬
            Collections.sort(hourlyList, Comparator
                        .comparingInt(ChooseSpotScore::getTotalScore)
                        .thenComparingInt(ChooseSpotScore::getPopularityScore)     // 인기점수
                        .thenComparingInt(ChooseSpotScore::getNearSpotScore)       // 근처 관광지 점수
                        .thenComparingInt(ChooseSpotScore::getRegularHoursScore)   // 시간점수
//                        .thenComparingInt(ChooseSpotScore::getTagScore)            // 태그점수
                        .thenComparingInt(ChooseSpotScore::getWatchedScore)        // 관람점수
                        .reversed());

            for (int i = 0; i < hourlyList.size(); i++) {
                hourlyList.get(i).setRanking(i + 1);
            }

            // 가장 높은 점수를 받은 장소 선택
            ChooseSpotScore selectedFirstSpot = hourlyList.get(0);
            System.out.println(selectedFirstSpot.getSpotName()+" >> "+selectedFirstSpot.getSpotTag());

            // Store hourly result in map
            hourlyScores.put(startTime, selectedFirstSpot);

            Map.Entry<LocalDateTime, ChooseSpotScore> maxEntry = hourlyScores.entrySet()
                    .stream()
                    .max(Comparator.comparing(entry -> entry.getValue().getTotalScore()))
                    .orElse(null);

            if (maxEntry != null) {
                LocalDateTime maxTime = maxEntry.getKey();
                startTime = maxTime;

                ChooseSpotScore maxScore = maxEntry.getValue();


                SaveSpotList.add(maxScore);

                // chooseSpotScoreList에서 이름이 maxSpotName과 같은 항목을 제거합니다.
                chooseSpotScoreList.removeIf(spot -> spot.getSpotName().equals(maxScore.SpotName));


                Datetotaltourtime = Datetotaltourtime+maxScore.getRegularTime();
//                startTime = startTime.plusMinutes(maxScore.getRegularTime());
            }


            Duration duration = Duration.between(startTime, endTime);
            int Datetourhours = (int) duration.toHours(); //하루 여행 시간의 시
            int Datetourminutes = (int) (duration.toMinutes() % 60); //허루 여행 시간의 분

            //하루 총 여행 시간
            int Datetourtime = (Datetourhours*60)+Datetourminutes;

            //하루 여행지 최대 관람 시간
            int MaxDatetourtime = (int) (Datetourtime*0.75);


            //하루 여행지 최소 관람 시간
            int MinDatetourtime = (int) (MaxDatetourtime*0.8);

            int selectnum=0;

//==================================================================================================================
            //2번째 여행지 선정할때 첫 여행지의 태그와 일치하면 태그 점수(20점)만큼 총점수 감소
            for (ChooseSpotScore spotScore : chooseSpotScoreList) {
                System.out.println(spotScore.getSpotName());
                if (selectedFirstSpot.getSpotTag().equals(spotScore.getSpotTag())) {
                    spotScore.setTotalScore(spotScore.getTotalScore()-spotScore.getTagScore());
                }
            }
            //태그 점수(20점)만큼 감소시킨후 정렬
            Collections.sort(chooseSpotScoreList, Comparator
                    .comparingInt(ChooseSpotScore::getTotalScore)
                    .thenComparingInt(ChooseSpotScore::getPopularityScore)     // 인기점수
                    .thenComparingInt(ChooseSpotScore::getNearSpotScore)       // 근처 관광지 점수
                    .thenComparingInt(ChooseSpotScore::getRegularHoursScore)   // 시간점수
//                        .thenComparingInt(ChooseSpotScore::getTagScore)            // 태그점수
                    .thenComparingInt(ChooseSpotScore::getWatchedScore)        // 관람점수
                    .reversed());


            // 가장 높은 총점의 여행지 선택
            ChooseSpotScore selectedSpot; // 리스트 첫 번째 (총점이 가장 높음)

            //첫 여행지 선정 이후 다음 여행지 선정
            while (!chooseSpotScoreList.isEmpty() && Datetotaltourtime <= MaxDatetourtime) {
                if (selectnum >= 0 && selectnum < chooseSpotScoreList.size()) {
                    selectedSpot = chooseSpotScoreList.get(selectnum);
                    System.out.println("선택된 다음 여행지 '" + selectedSpot.getSpotName());
                    // 선택한 여행지의 소요 시간
                    int travelTime = selectedSpot.getRegularTime(); // 소요 시간

                    // Datetotaltourtime이 MaxDatetourtime을 넘지 않는 경우만 처리
                    if (Datetotaltourtime+travelTime <= MaxDatetourtime) {

                        // 선택한 여행지의 소요 시간을 Datetotaltourtime에 더함
                        Datetotaltourtime = Datetotaltourtime+travelTime;


                        SaveSpotList.add(selectedSpot);

                        // 리스트에서 제거
                        chooseSpotScoreList.remove(selectnum);

                    } else if (Datetotaltourtime+travelTime>=MaxDatetourtime && Datetotaltourtime<=MinDatetourtime) {
                        if(selectnum+1<chooseSpotScoreList.size()) {
                            selectnum += 1;

//==================================================================================================================
                            //다음 여행지 선정할때 첫 여행지의 태그와 일치하면 태그 점수(20점)만큼 총점수 감소
                            for (ChooseSpotScore spotScore : chooseSpotScoreList) {
                                if (selectedSpot.getSpotTag().equals(spotScore.getSpotTag())) {
                                    spotScore.setTotalScore(spotScore.getTotalScore()-spotScore.getTagScore());
                                }
                            }
                            //태그 점수(20점)만큼 감소시킨후 정렬
                            Collections.sort(chooseSpotScoreList, Comparator
                                    .comparingInt(ChooseSpotScore::getTotalScore)
                                    .thenComparingInt(ChooseSpotScore::getPopularityScore)     // 인기점수
                                    .thenComparingInt(ChooseSpotScore::getNearSpotScore)       // 근처 관광지 점수
                                    .thenComparingInt(ChooseSpotScore::getRegularHoursScore)   // 시간점수
//                        .thenComparingInt(ChooseSpotScore::getTagScore)            // 태그점수
                                    .thenComparingInt(ChooseSpotScore::getWatchedScore)        // 관람점수
                                    .reversed());
                            
                        }else{
                            break;
                        }
                    }else {
                        // 시간 초과로 제외
                        System.out.println("하루 여행지 최대 관람 시간을 넘었습니다.");
                        System.out.println("선택된 다음 여행지 '" + selectedSpot.getSpotName() + "'은(는) 시간 초과로 제외됩니다.");
                        break;
                    }
                } else {
                    break;
                }


            }

            FinalChooseSpotMap.put(startTime,findMatchingSpots(TravelSpotArray,SaveSpotList));

            if (chooseSpotScoreList.isEmpty()) {
                System.out.println("chooseSpotScoreList가 비었습니다.");
                break;
            }


        }


        return FinalChooseSpotMap;
    }

    public JsonObject ChooseRestaurant(double currentLat, double currentLng,String mapapikey) throws IOException {
//        Map<LocalDateTime, JsonArray> FinalChooseSpotMap = new LinkedHashMap<>();

        String Lat = String.valueOf(currentLat);
        String Lng = String.valueOf(currentLng);


        JsonArray RestaurantSpotArray = foursquareRequest.RestaurantReq(foursquareService.printApiKey(),gptApiCompent.printApiKey(),Lat,Lng);
        JsonObject place = null;


        for (int i = 0; i < RestaurantSpotArray.size(); i++) {
            boolean placecheck;
            place = RestaurantSpotArray.get(i).getAsJsonObject();
            try {
                placecheck = directionsController.hasPlaceID(mapapikey, place.get("name").getAsString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (!placecheck) {
                System.out.println("Skipping place: " + place.get("name").getAsString() + " (Place ID not found)");
                continue; // 아래 코드는 실행되지 않고, 다음 반복으로 넘어감
            }

            return place;
        }
        
        return place;
    }


}
