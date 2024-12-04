package com.sw.springboot.FoursquareAPI;


import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FoursquarePhoto {

    public String foursquarephoto(JsonObject place,String fsq_id,String FoursquareApiKey) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.foursquare.com/v3/places/"+fsq_id+"/photos?sort=POPULAR")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", FoursquareApiKey)
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        // Gson을 사용하여 JSON 응답을 파싱
        JsonArray photos = JsonParser.parseString(responseBody).getAsJsonArray();
        StringBuilder photoUrls = new StringBuilder();

        if (photos != null && !photos.isEmpty()){
            JsonObject photo = photos.get(0).getAsJsonObject();
            String prefix = photo.get("prefix").getAsString();
            String suffix = photo.get("suffix").getAsString();

            // 전체 이미지 URL 생성
            String photoUrl = prefix+"150x150"+ suffix;

            // 결과를 StringBuilder에 추가
            photoUrls.append(photoUrl).append("\n");
        }
        else{
            System.out.println("photos 없음!!!!!!!!!!!!!!!!!!!!!!!");

            JsonObject icon = place
                    .get("categories")
                    .getAsJsonArray()
                    .get(0).getAsJsonObject();

            // Combine prefix and suffix
            String iconUrl = icon.get("prefix")+"150"+icon.get("suffix");
            photoUrls.append(iconUrl).append("\n");
        }

        return photoUrls.toString();
    }
}
