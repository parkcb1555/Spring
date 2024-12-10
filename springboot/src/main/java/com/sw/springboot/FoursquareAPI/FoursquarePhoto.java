package com.sw.springboot.FoursquareAPI;


import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FoursquarePhoto {

    public String foursquarephoto(JsonObject place,String fsq_id,String FoursquareApiKey,String type) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.foursquare.com/v3/places/"+fsq_id+"/photos?sort=POPULAR")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", FoursquareApiKey)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        System.out.println(responseBody);  // 응답 내용 출력
        // Gson을 사용하여 JSON 응답을 파싱
//        JsonArray photos = JsonParser.parseString(responseBody).getAsJsonArray();
//        StringBuilder photoUrls = new StringBuilder();
//
//        if (photos != null && !photos.isEmpty()){
//            JsonObject photo = photos.get(0).getAsJsonObject();
//            String prefix = photo.get("prefix").getAsString();
//            String suffix = photo.get("suffix").getAsString();
//
//            // 전체 이미지 URL 생성
//            String photoUrl = prefix+"150x150"+ suffix;
//
//            // 결과를 StringBuilder에 추가
//            photoUrls.append(photoUrl).append("\n");
//        }
//        else{
//            System.out.println("photos 없음!!!!!!!!!!!!!!!!!!!!!!!");
//
//            JsonObject icon = place
//                    .get("categories")
//                    .getAsJsonArray()
//                    .get(0).getAsJsonObject();
//
//            // Combine prefix and suffix
//            String iconUrl = icon.get("prefix")+"150"+icon.get("suffix");
//            photoUrls.append(iconUrl).append("\n");
//        }
        StringBuilder photoUrls = new StringBuilder();
        try {
            // responseBody에 "invalid venue specified" 메시지가 포함된 경우를 체크
            if (!responseBody.contains("invalid venue specified")) {
                JsonArray photos = JsonParser.parseString(responseBody).getAsJsonArray();
                photoUrls = new StringBuilder();

                if (photos != null && photos.size() > 0) {
                    JsonObject photo = photos.get(0).getAsJsonObject();
                    String prefix = photo.get("prefix").getAsString();
                    String suffix = photo.get("suffix").getAsString();

                    // 전체 이미지 URL 생성
                    String photoUrl = prefix + "150x150" + suffix;
                    photoUrls.append(photoUrl).append("\n");
                } else {
                    System.out.println("photos 없음!!!!!!!!!!!!!!!!!!!!!!!");

                    // 사진이 없을 경우 카테고리 아이콘 사용
                    JsonObject icon = place
                            .get("categories")
                            .getAsJsonArray()
                            .get(0).getAsJsonObject();
        
                    // Combine prefix and suffix
                    String iconUrl = icon.get("prefix")+"150"+icon.get("suffix");
                    photoUrls.append(iconUrl).append("\n");
                }
            } else {
                System.out.println("Venue 정보가 잘못되었습니다: invalid venue specified");

                if(type.equals("spot")){
                    String iconUrl = "https://ss3.4sqi.net/img/categories_v2/landmarks_outdoors/landmarks_outdoors_120.png";
                    photoUrls.append(iconUrl).append("\n");
                }else if(type.equals("restaurant")){
                    String iconUrl = "https://ss3.4sqi.net/img/categories_v2/food/restaurant_120.png";
                    photoUrls.append(iconUrl).append("\n");
                }
            }
        } catch (JsonSyntaxException e) {
            System.out.println("JSON 파싱 오류 발생: " + e.getMessage());
            // JSON 형식에 문제가 있을 경우 예외 처리
        } catch (Exception e) {
            System.out.println("기타 오류 발생: " + e.getMessage());
        }

        return photoUrls.toString();
    }
}
