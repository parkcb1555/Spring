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

        @Setter
        int RegularTime;

        // 총점 필드
        @Setter
        private int totalScore;

    }

    @Autowired
    foursquareRequest foursquareRequest;


    public void FirstTravelSpot(JsonArray sortedResultsArray){
        System.out.println("Received JSON Array: " + sortedResultsArray.toString());

    }

    // 관광지 기본 점수 계산
    public int calculateTotalScore(ChooseSpotScore score) {
        return score.PopularityScore + score.WatchedScore  + score.NearSpotScore;
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


    @GetMapping("/ChooseTravelSpot")
    public Map<LocalDateTime, JsonArray> ChooseTravelSpot(String city,String[] tags, String startdate, String enddate, Map<String, String[]> dateTimeMap,String mapapikey) throws IOException {
        System.out.println("ChooseTravelSpot 접속");
        ArrayList<ChooseSpotScore> chooseSpotScoreList = new ArrayList<>();
        ChooseSpotScore chooseSpotScore;

        String Category = CategorySetting(tags);


        //최종적으로 return 할 Map
        Map<LocalDateTime, JsonArray> FinalChooseSpotMap = new LinkedHashMap<>();

        LocalDate start = LocalDate.parse(startdate);
        LocalDate end = LocalDate.parse(enddate);
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;




        JsonArray TravelSpotArray = foursquareRequest.req(foursquareService.printApiKey(),gptApiCompent.printApiKey(),Category,city);


        //여행지 기본 점수 설정
        for (int i = 0; i < TravelSpotArray.size(); i++) {
            int random1to100 = (int) (Math.random() * 100) + 1;
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

            chooseSpotScore.setPopularityScore(random1to100);

            random1to100 = (int) (Math.random() * 100) + 1;
            chooseSpotScore.setTagScore(random1to100);

            random1to100 = (int) (Math.random() * 100) + 1;
            chooseSpotScore.setWatchedScore(random1to100);

            random1to100 = (int) (Math.random() * 100) + 1;
            chooseSpotScore.setNearSpotScore(random1to100);

            random1to100 = (int) (Math.random() * 100) + 1;
            chooseSpotScore.setRegularHoursScore(random1to100);

            chooseSpotScore.setTotalScore(calculateTotalScore(chooseSpotScore));
            chooseSpotScoreList.add(chooseSpotScore);
        }

        Collections.sort(chooseSpotScoreList, Comparator
                .comparingInt(this::calculateTotalScore)
                .thenComparingInt(ChooseSpotScore::getPopularityScore)     // 인기점수
                .thenComparingInt(ChooseSpotScore::getNearSpotScore)       // 근처 관광지 점수
                .thenComparingInt(ChooseSpotScore::getRegularHoursScore)   // 시간점수
                .thenComparingInt(ChooseSpotScore::getTagScore)            // 태그점수
                .thenComparingInt(ChooseSpotScore::getWatchedScore)        // 관람점수
                .reversed());

        for (int i = 0; i < chooseSpotScoreList.size(); i++) {
            chooseSpotScoreList.get(i).setRanking(i + 1);
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


            //첫 여행지 선정
                 ArrayList<ChooseSpotScore> hourlyList = new ArrayList<>();

                //첫 여행지 점수 설정
                for (ChooseSpotScore spotScore : chooseSpotScoreList) {
                    ChooseSpotScore hourlySpotScore = new ChooseSpotScore();
                    hourlySpotScore.setSpotName(spotScore.getSpotName());
                    hourlySpotScore.setPopularityScore(spotScore.getPopularityScore());
                    hourlySpotScore.setTagScore(spotScore.getTagScore());
                    hourlySpotScore.setWatchedScore(spotScore.getWatchedScore());
                    hourlySpotScore.setNearSpotScore(spotScore.getNearSpotScore());
                    hourlySpotScore.setRegularHoursScore(spotScore.getRegularHoursScore());
                    hourlySpotScore.setTotalScore(spotScore.getTotalScore());
                    hourlySpotScore.setRanking(spotScore.getRanking());
                    hourlySpotScore.setRegularTime(spotScore.getRegularTime());

                    // Generate new random score for RegularHoursScore each hour
                    int randomHourlyScore = (int) (Math.random() * 100) + 1;
                    hourlySpotScore.setRegularHoursScore(0-(HourpenaltyCount*20));
                    // Calculate total score including hourly score
                    int firstTotalScore = calculateTotalScore(hourlySpotScore) + randomHourlyScore;
                    hourlySpotScore.setTotalScore(firstTotalScore);
                    hourlyList.add(hourlySpotScore);
                }

                // Sort and rank the hourly list
                Collections.sort(hourlyList, Comparator
                        .comparingInt(ChooseSpotScore::getTotalScore)
                        .thenComparingInt(ChooseSpotScore::getPopularityScore)     // 인기점수
                        .thenComparingInt(ChooseSpotScore::getNearSpotScore)       // 근처 관광지 점수
                        .thenComparingInt(ChooseSpotScore::getRegularHoursScore)   // 시간점수
                        .thenComparingInt(ChooseSpotScore::getTagScore)            // 태그점수
                        .thenComparingInt(ChooseSpotScore::getWatchedScore)        // 관람점수
                        .reversed());

                for (int i = 0; i < hourlyList.size(); i++) {
                    hourlyList.get(i).setRanking(i + 1);
                }

                // 가장 높은 점수를 받은 장소 선택
                ChooseSpotScore selectedFirstSpot = hourlyList.get(0);

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
