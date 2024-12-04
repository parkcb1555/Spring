package com.sw.springboot.FoursquareAPI;
import java.io.IOException;

import com.sw.springboot.FoursquareAPI.FoursquarePhoto.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import okhttp3.*;
import okhttp3.OkHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.util.*;


import com.sw.springboot.GptAPI.*;
import com.sw.springboot.GptAPI.GPT_API;
import com.sw.springboot.GptAPI.GPT_API_Compent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class foursquareRequest {

    @Autowired
    GPT_API gpt_api;

    @GetMapping("/foursquare")
    public JsonArray req(String FoursquareApiKey,String gptapikey,String tag,String city) throws IOException {
        OkHttpClient client = new OkHttpClient();

        System.out.println(tag+"   "+city);



        double originLat=0.0;
        double originLng=0.0;
        double destinationLat=0.0;
        double destinationLng=0.0;

        // Foursquare Place Search API 요청 (인기도 순 정렬 및 카테고리 ID 포함)
        Request request = new Request.Builder()
                .url("https://api.foursquare.com/v3/places/search?categories="+tag+"&sort=RATING&limit=20&near="+city+"&fields=name,fsq_id,location,categories,geocodes,hours,price,popularity,rating,stats,tastes")
                .get()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", FoursquareApiKey)  // 발급받은 API 키 사용
                .build();

        // API 응답 받기
        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();


        // 응답 데이터를 JSON으로 파싱
        JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
        JsonArray resultsArray = jsonObject.getAsJsonArray("results");


        // JsonArray -> List<JsonObject> 변환
        List<JsonObject> placesList = new ArrayList<>();
        for (int i = 0; i < resultsArray.size(); i++) {
            placesList.add(resultsArray.get(i).getAsJsonObject());
        }


        //stats가 null인 것들은 이름 순으로
        placesList.sort((place1, place2) -> {
            JsonObject stats1 = place1.getAsJsonObject("stats");
            JsonObject stats2 = place2.getAsJsonObject("stats");

            // stats1과 stats2가 null인 경우 처리
            if (stats1 == null && stats2 == null) {
                // 두 개가 모두 null일 경우 이름을 비교하여 정렬
                return place1.get("name").getAsString().compareTo(place2.get("name").getAsString());
            } else if (stats1 == null) {
                // stats1이 null인 경우 stats2 기준으로 이름 정렬
                return place1.get("name").getAsString().compareTo(place2.get("name").getAsString());
            } else if (stats2 == null) {
                // stats2가 null인 경우 stats1 기준으로 이름 정렬
                return place1.get("name").getAsString().compareTo(place2.get("name").getAsString());
            }

            // stats가 null이 아닐 경우 리뷰 수와 별점 가져오기
            int reviewCount1 = stats1.has("total_ratings") ? stats1.get("total_ratings").getAsInt() : 0;
            int tips1 = stats1.has("total_tips") ? stats1.get("total_tips").getAsInt() : 0;
            int reviewCount2 = stats2.has("total_ratings") ? stats2.get("total_ratings").getAsInt() : 0;
            int tips2 = stats2.has("total_tips") ? stats2.get("total_tips").getAsInt() : 0;

            // 리뷰 수와 좋아요 수의 합 계산
            int total1 = reviewCount1 + tips1;
            int total2 = reviewCount2 + tips2;

            // 합이 높은 순으로 정렬 (내림차순)
            return Integer.compare(total2, total1);
        });

// 다시 List<JsonObject> -> JsonArray 변환
        JsonArray sortedResultsArray = new JsonArray();
        for (JsonObject place : placesList) {
            sortedResultsArray.add(place);
        }

        // 정렬된 결과 출력
        for (int i = 0; i < sortedResultsArray.size(); i++) {

//            System.out.println((i+1)+"번째");
            JsonObject place = sortedResultsArray.get(i).getAsJsonObject();


            String name = place.get("name").getAsString();
            JsonObject location = place.getAsJsonObject("location");

            String latitude = "";
            String longitude = "";
            // 지리 정보 확인
            if (place.has("geocodes")) {
                JsonObject geocodes = place.getAsJsonObject("geocodes").getAsJsonObject("main"); // 'main' 키 사용
                latitude = String.valueOf(geocodes.has("latitude") ? geocodes.get("latitude").getAsDouble() : 0.0);
                longitude = String.valueOf(geocodes.has("longitude") ? geocodes.get("longitude").getAsDouble() : 0.0);
            }

            String address = "주소 정보 없음";
            if (location.has("address")) {
                address = location.get("address").getAsString();
            }else{
                address = gpt_api.Gpt_Request(gptapikey,latitude,longitude,name,"address");
                location.addProperty("address",address);
            }

//            address = location.has("address") ? location.get("address").getAsString() : gpt_api.Gpt_Request(name,"address");

            //시도 추출
            String region = location.has("region") ? location.get("region").getAsString() : "region 정보 없음";


//
//                System.out.println("장소 이름: " + place.get("name").getAsString());
//                System.out.println("위도: " + latitude);
//                System.out.println("경도: " + longitude);
//                System.out.println(); // 추가적인 줄바꿈
//                if (originLat == 0.0){
//                    originLat = Double.parseDouble(latitude);
//                    originLng = Double.parseDouble(longitude);
//                }else {
//                    destinationLat = Double.parseDouble(latitude);
//                    destinationLng = Double.parseDouble(longitude);
//                }
//
//            } else {
//                System.out.println("geocodes 정보가 없습니다.");
//            }



            String regularHours = "영업 시간 없음";
            if (place.has("hours")) {
                JsonObject hours = place.getAsJsonObject("hours");
                if (hours.has("regular")) {
                    JsonArray regularArray = hours.getAsJsonArray("regular");
                    StringBuilder hoursBuilder = new StringBuilder();
                    for (int j = 0; j < regularArray.size(); j++) {
                        JsonObject dayInfo = regularArray.get(j).getAsJsonObject();
                        int day = dayInfo.get("day").getAsInt();
                        String openTime = dayInfo.get("open").getAsString();
                        String closeTime = dayInfo.get("close").getAsString();
                        hoursBuilder.append("요일: ").append(getDayName(day))
                                .append(", 오픈: ").append(openTime)
                                .append(", 마감: ").append(closeTime).append("\n");
                    }
                    regularHours = hoursBuilder.toString();
                }else{
                    ObjectMapper objectMapper = new ObjectMapper();

                    String string_rh = gpt_api.Gpt_Request(gptapikey,latitude,longitude,name,"regularHours");
                    String string_rh_GPT = regularHours_GPT(string_rh);

                    regularHours = string_rh_GPT;
                    // 주석 제거: "//"로 시작하는 주석을 정규 표현식으로 제거
                    String cleanedRegularHours = regularHours.replaceAll("//.*", "");

                    // Gson 객체를 이용해 cleanedRegularHours 문자열을 JsonObject로 변환
                    Gson gson = new Gson();
                    JsonObject jsonObject2 = gson.fromJson(cleanedRegularHours, JsonObject.class);

                    // "regular" 배열을 JsonArray로 추출
                    JsonArray regularHoursArray = jsonObject2.getAsJsonArray("regular");


                    hours.add("regular", regularHoursArray);

                }

            }


            String priceTier;
            if(place.has("price")){
                priceTier = place.get("price").getAsString();
            }else{
                priceTier = gpt_api.Gpt_Request(gptapikey,latitude,longitude,name,"priceTier");
                place.addProperty("price", priceTier);
            }

            if( place.has("rating")){
                place.get("rating").getAsString();
            }else{
                place.addProperty("rating", 0.0);
            }

            String statsInfo = "리뷰 및 평점 수 정보 없음";
            if (place.has("stats")) {
                JsonObject stats = place.getAsJsonObject("stats");
                int ratingsCount = stats.has("total_ratings") ? stats.get("total_ratings").getAsInt() : 0;
                int tips = stats.has("total_tips") ? stats.get("total_tips").getAsInt() : 0;
                statsInfo = "평점 수: " + ratingsCount + ", 리뷰/팁 수: " + tips;

                if (!stats.has("total_ratings")){
                    stats.addProperty("total_ratings", 0);
                }
                if (!stats.has("total_tips")){
                    stats.addProperty("total_tips", 0);
                }

            } else {
                // "stats" 객체가 없으면 새로 생성하여 추가
                JsonObject stats = new JsonObject();
                stats.addProperty("total_ratings", 0);  // 기본 값 설정
                stats.addProperty("total_tips", 0);     // 기본 값 설정
                place.add("stats", stats);  // "stats" 객체를 place에 추가
            }

            String categoriesInfo = "카테고리 정보 없음";
            if (place.has("categories")) {
                JsonArray categoriesArray = place.getAsJsonArray("categories");
                StringBuilder categoriesBuilder = new StringBuilder();
                for (int j = 0; j < categoriesArray.size(); j++) {
                    JsonObject category = categoriesArray.get(j).getAsJsonObject();
                    String categoryName = category.get("name").getAsString();
                    String categoryId = category.get("id").getAsString();
                    // 카테고리 ID가 어떤 태그에 속하는지 확인
                    String placetag = findCategoryTag(categoryId);
                    place.addProperty("tag",placetag);

                    categoriesBuilder.append("ID: ").append(categoryId).append(", 이름: ").append(categoryName);
                    if (j < categoriesArray.size() - 1) {
                        categoriesBuilder.append("; ");
                    }
                }
                categoriesInfo = categoriesBuilder.toString();
            }

            //평균 관람 시간 구하기
            int AverageTime = GPT_API.AverageTime(gptapikey,latitude,longitude,name); //평균 관람 시간 구하기
            place.addProperty("RegularTime",AverageTime);

            String PlaceDescription = GPT_API.PlaceChooseReason(gptapikey,latitude,longitude,name);
            place.addProperty("PlaceDescription",PlaceDescription);

            FoursquarePhoto foursquarePhoto = new FoursquarePhoto();
            String photourl = foursquarePhoto.foursquarephoto(place,place.get("fsq_id").getAsString(),FoursquareApiKey);
            place.addProperty("photourl",photourl);

        }


        return sortedResultsArray;

    }

    @GetMapping("/RestaurantReq")
    public JsonArray RestaurantReq(String FoursquareApiKey,String gptapikey,String Lat,String Lng) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Foursquare Place Search API 요청 (인기도 순 정렬 및 카테고리 ID 포함)
        Request request = new Request.Builder()
                .url("https://api.foursquare.com/v3/places/search?ll="+Lat+","+Lng+"&radius=2000&categories=13065&sort=RATING&limit=5&fields=name,fsq_id,location,categories,geocodes,hours,price,popularity,rating,stats,tastes")
                .get()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", FoursquareApiKey)  // 발급받은 API 키 사용
                .build();

        // API 응답 받기
        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();


        // 응답 데이터를 JSON으로 파싱
        JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
        JsonArray resultsArray = jsonObject.getAsJsonArray("results");


        // JsonArray -> List<JsonObject> 변환
        List<JsonObject> placesList = new ArrayList<>();
        for (int i = 0; i < resultsArray.size(); i++) {
            placesList.add(resultsArray.get(i).getAsJsonObject());
        }


        //stats가 null인 것들은 이름 순으로
        placesList.sort((place1, place2) -> {
            JsonObject stats1 = place1.getAsJsonObject("stats");
            JsonObject stats2 = place2.getAsJsonObject("stats");

            // stats1과 stats2가 null인 경우 처리
            if (stats1 == null && stats2 == null) {
                // 두 개가 모두 null일 경우 이름을 비교하여 정렬
                return place1.get("name").getAsString().compareTo(place2.get("name").getAsString());
            } else if (stats1 == null) {
                // stats1이 null인 경우 stats2 기준으로 이름 정렬
                return place1.get("name").getAsString().compareTo(place2.get("name").getAsString());
            } else if (stats2 == null) {
                // stats2가 null인 경우 stats1 기준으로 이름 정렬
                return place1.get("name").getAsString().compareTo(place2.get("name").getAsString());
            }

            // stats가 null이 아닐 경우 리뷰 수와 별점 가져오기
            int reviewCount1 = stats1.has("total_ratings") ? stats1.get("total_ratings").getAsInt() : 0;
            int tips1 = stats1.has("total_tips") ? stats1.get("total_tips").getAsInt() : 0;
            int reviewCount2 = stats2.has("total_ratings") ? stats2.get("total_ratings").getAsInt() : 0;
            int tips2 = stats2.has("total_tips") ? stats2.get("total_tips").getAsInt() : 0;

            // 리뷰 수와 좋아요 수의 합 계산
            int total1 = reviewCount1 + tips1;
            int total2 = reviewCount2 + tips2;

            // 합이 높은 순으로 정렬 (내림차순)
            return Integer.compare(total2, total1);
        });

// 다시 List<JsonObject> -> JsonArray 변환
        JsonArray sortedResultsArray = new JsonArray();
        for (JsonObject place : placesList) {
            sortedResultsArray.add(place);
        }

        // 정렬된 결과 출력
        for (int i = 0; i < sortedResultsArray.size(); i++) {

//            System.out.println((i+1)+"번째");
            JsonObject place = sortedResultsArray.get(i).getAsJsonObject();


            String name = place.get("name").getAsString();
            JsonObject location = place.getAsJsonObject("location");
            String latitude = "";
            String longitude = "";
            // 지리 정보 확인
            if (place.has("geocodes")) {
                JsonObject geocodes = place.getAsJsonObject("geocodes").getAsJsonObject("main"); // 'main' 키 사용
                latitude = String.valueOf(geocodes.has("latitude") ? geocodes.get("latitude").getAsDouble() : 0.0);
                longitude = String.valueOf(geocodes.has("longitude") ? geocodes.get("longitude").getAsDouble() : 0.0);
            }


            String address = "주소 정보 없음";
            if (location.has("address")) {
                address = location.get("address").getAsString();
            }else{
                address = gpt_api.Gpt_Request(gptapikey,latitude,longitude,name,"address");
                location.addProperty("address",address);
            }

            String regularHours = "영업 시간 없음";
            if (place.has("hours")) {
                JsonObject hours = place.getAsJsonObject("hours");
                if (hours.has("regular")) {
                    JsonArray regularArray = hours.getAsJsonArray("regular");
                    StringBuilder hoursBuilder = new StringBuilder();
                    for (int j = 0; j < regularArray.size(); j++) {
                        JsonObject dayInfo = regularArray.get(j).getAsJsonObject();
                        int day = dayInfo.get("day").getAsInt();
                        String openTime = dayInfo.get("open").getAsString();
                        String closeTime = dayInfo.get("close").getAsString();
                        hoursBuilder.append("요일: ").append(getDayName(day))
                                .append(", 오픈: ").append(openTime)
                                .append(", 마감: ").append(closeTime).append("\n");
                    }
                    regularHours = hoursBuilder.toString();
                }else{
                    ObjectMapper objectMapper = new ObjectMapper();

                    String string_rh = gpt_api.Gpt_Request(gptapikey,latitude,longitude,name,"regularHours");
                    String string_rh_GPT = regularHours_GPT(string_rh);

                    regularHours = string_rh_GPT;
                    // 주석 제거: "//"로 시작하는 주석을 정규 표현식으로 제거
                    String cleanedRegularHours = regularHours.replaceAll("//.*", "");

                    // Gson 객체를 이용해 cleanedRegularHours 문자열을 JsonObject로 변환
                    Gson gson = new Gson();
                    JsonObject jsonObject2 = gson.fromJson(cleanedRegularHours, JsonObject.class);
                    JsonNode hoursNode = objectMapper.readTree(place.toString()).get("hours");

                    // "regular" 배열을 JsonArray로 추출
                    JsonArray regularHoursArray = jsonObject2.getAsJsonArray("regular");


                    hours.add("regular", regularHoursArray);

                }

            }


            String priceTier;
            if(place.has("price")){
                priceTier = place.get("price").getAsString();
            }else{
                priceTier = gpt_api.Gpt_Request(gptapikey,latitude,longitude,name,"RestaurantpriceTier");
                place.addProperty("price", Integer.parseInt(priceTier));
            }


            if( place.has("rating")){
                place.get("rating").getAsString();
            }else{

                place.addProperty("rating", 0.0);
            }

            String statsInfo = "리뷰 및 평점 수 정보 없음";
            if (place.has("stats")) {
                JsonObject stats = place.getAsJsonObject("stats");
                int ratingsCount = stats.has("total_ratings") ? stats.get("total_ratings").getAsInt() : 0;
                int tips = stats.has("total_tips") ? stats.get("total_tips").getAsInt() : 0;
                statsInfo = "평점 수: " + ratingsCount + ", 리뷰/팁 수: " + tips;

                if (!stats.has("total_ratings")){
                    System.out.println("addProperty!!!!! total_ratings");
                    stats.addProperty("total_ratings", 0);
                }
                if (!stats.has("total_tips")){
                    System.out.println("addProperty!!!!! total_tips");
                    stats.addProperty("total_tips", 0);
                }
            } else {
                // "stats" 객체가 없으면 새로 생성하여 추가
                JsonObject stats = new JsonObject();
                stats.addProperty("total_ratings", 0);  // 기본 값 설정
                stats.addProperty("total_tips", 0);     // 기본 값 설정
                place.add("stats", stats);  // "stats" 객체를 place에 추가
            }

            String categoriesInfo = "카테고리 정보 없음";
            if (place.has("categories")) {
                JsonArray categoriesArray = place.getAsJsonArray("categories");
                StringBuilder categoriesBuilder = new StringBuilder();
                for (int j = 0; j < categoriesArray.size(); j++) {
                    JsonObject category = categoriesArray.get(j).getAsJsonObject();
                    String categoryName = category.get("name").getAsString();
                    String categoryId = category.get("id").getAsString();
//                    // 카테고리 ID가 어떤 태그에 속하는지 확인
//                    String tag = findCategoryTag(categoryId);
//                    System.out.println(tag);
//                    place.addProperty("tag",tag);
                    categoriesBuilder.append("ID: ").append(categoryId).append(", 이름: ").append(categoryName);
                    if (j < categoriesArray.size() - 1) {
                        categoriesBuilder.append("; ");
                    }
                }
                categoriesInfo = categoriesBuilder.toString();
            }

            int RegularTime = 60;
            place.addProperty("RegularTime",RegularTime);

            String PlaceDescription = GPT_API.PlaceChooseReason(gptapikey,latitude,longitude,name);
            place.addProperty("PlaceDescription",PlaceDescription);

            FoursquarePhoto foursquarePhoto = new FoursquarePhoto();
            String photourl = foursquarePhoto.foursquarephoto(place,place.get("fsq_id").getAsString(),FoursquareApiKey);
            place.addProperty("photourl",photourl);

        }


        return sortedResultsArray;

    }


    // 요일 숫자를 요일 이름으로 변환하는 메서드
    private static String getDayName(int day) {
        switch (day) {
            case 1: return "월요일";
            case 2: return "화요일";
            case 3: return "수요일";
            case 4: return "목요일";
            case 5: return "금요일";
            case 6: return "토요일";
            case 7: return "일요일";
            default: return "알 수 없음";
        }
    }

    private static String regularHours_GPT(String gt) {
        // JSON 문자열
        String jsonString = """
        {
                %s
        }""".formatted(gt);;


        // 기본 값 설정
        String regularHours = "영업 시간 없음";
        JsonObject place = JsonParser.parseString(jsonString).getAsJsonObject();

        // "hours" 객체가 있는지 확인
        if (place.has("hours")) {
            JsonObject hours = place.getAsJsonObject("hours");
            // "regular" 배열이 있는지 확인
            if (hours.has("regular")) {
                JsonArray regularArray = hours.getAsJsonArray("regular");
                StringBuilder hoursBuilder = new StringBuilder();
                for (int j = 0; j < regularArray.size(); j++) {
                    JsonObject dayInfo = regularArray.get(j).getAsJsonObject();
                    int day = dayInfo.get("day").getAsInt();
                    String openTime = dayInfo.get("open").getAsString();
                    String closeTime = dayInfo.get("close").getAsString();
                    hoursBuilder.append("요일: ").append(getDayName(day))
                            .append(", 오픈: ").append(openTime)
                            .append(", 마감: ").append(closeTime).append("\n");
                }
                // StringBuilder의 내용을 regularHours에 저장
                regularHours = hoursBuilder.toString().trim(); // 마지막 줄바꿈 제거
            }
        }

        return jsonString;
    }


    // 태그와 카테고리 ID 매핑을 생성
    public static Map<String, String[]> createTagCategoryMap() {
        Map<String, String[]> tagCategoryMap = new HashMap<>();
        tagCategoryMap.put("Historic", new String[]{"12099", "12102", "12111", "16011", "16020", "16031"});
        tagCategoryMap.put("Landmarks", new String[]{"16024", "16025", "16026", "16046"});
        tagCategoryMap.put("Shopping", new String[]{"17030", "17033", "17036", "17089", "17104", "17105", "17109", "17114", "17115", "17116"});
        tagCategoryMap.put("Market", new String[]{"17002", "17054", "17144"});
        tagCategoryMap.put("Resort", new String[]{"19012", "19016", "19018"});
        tagCategoryMap.put("Nature", new String[]{"16002", "16003", "16005", "16009", "16023", "16028", "16030", "16042", "16043", "16053"});
        tagCategoryMap.put("Park", new String[]{"16033", "16034", "16035", "16036", "16037", "16038", "16039", "16047", "16060"});
        tagCategoryMap.put("Casino", new String[]{"10003", "10005", "10008", "10033"});
        tagCategoryMap.put("Spa", new String[]{"16021", "18081"});
        tagCategoryMap.put("Art", new String[]{"10004", "10016", "10028", "10030"});
        tagCategoryMap.put("Entertainment", new String[]{"10001", "10002", "10015", "10019", "10022", "10044", "10055", "10056"});
        tagCategoryMap.put("Cafe", new String[]{"13033", "13034", "13035", "13036", "13063", "13381"});

        return tagCategoryMap;
    }

    // 카테고리 ID가 어떤 태그에 속하는지 확인
    public static String findCategoryTag(String categoryId) {
        Map<String, String[]> tagCategoryMap = createTagCategoryMap();

        for (Map.Entry<String, String[]> entry : tagCategoryMap.entrySet()) {
            String tag = entry.getKey();
            String[] categoryIds = entry.getValue();

            for (String id : categoryIds) {
                if (id.equals(categoryId)) {
                    return tag; // 해당 태그 반환
                }
            }
        }

        return "Unknown"; // 해당하지 않는 경우
    }
}
