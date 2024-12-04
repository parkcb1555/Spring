package com.sw.springboot.GooGlePlaceAPI;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.*;
import com.google.maps.model.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

//@Service
//public class DirectionsService {
//    private final GeoApiContext context;
//
//    public DirectionsService(@Value("${google.api.key}") String apiKey) {
//        this.context = new GeoApiContext.Builder()
//                .apiKey(apiKey)
//                .build();
//    }
//
//    public DirectionsResult getDirections(String origin, String destination, List<String> waypoints) throws Exception {
//        // 경유지가 있을 경우
//        if (waypoints != null && !waypoints.isEmpty()) {
//            return DirectionsApi.newRequest(context)
//                    .origin(origin)
//                    .destination(destination)
//                    .waypoints(waypoints.toArray(new String[0])) // String 배열로 변환
//                    .await();
//        } else {
//            // 경유지가 없을 경우
//            return DirectionsApi.newRequest(context)
//                    .origin(origin)
//                    .destination(destination)
//                    .await();
//        }
//    }
//}

@Getter
@Service
public class DirectionsService {
    private final GeoApiContext context;

    String key;

    public String printApiKey() {

        return key;
    }

    public DirectionsService(@Value("${google.api.key}") String apikey) {
        this.key = apikey;
        this.context = new GeoApiContext.Builder()
                .apiKey(key)
                .build();
    }

    //장소 위도,경도로 이름으로 길찾기
    public DirectionsResult getDirections(double originLat, double originLng, double destinationLat, double destinationLng)  throws Exception {
        return DirectionsApi.newRequest(context)
                .mode(TravelMode.TRANSIT) // 운전 모드, 필요에 따라 변경 가능
                .origin(new LatLng(originLat,originLng))
                .destination(new LatLng(destinationLat,destinationLng))
//                .alternatives(true)
                .language("ko")
                .await();
    }

    //장소 이름으로 길찾기
    public DirectionsResult getNameDirections(String origin,String destination)  throws Exception {

        String originPlaceId = getPlaceId(getKey(),origin);
        String destinationPlaceId = getPlaceId(getKey(),destination);

        return DirectionsApi.newRequest(context)
                .mode(TravelMode.TRANSIT) // 운전 모드, 필요에 따라 변경 가능
                .origin("place_id:"+originPlaceId)
                .destination("place_id:"+destinationPlaceId)
//                .origin(origin)
//                .destination(destination)
                .language("ko")
                .await();
    }
    public String getPlaceId(String apikey,String name) throws Exception {
        System.out.println(name);
        // GeoApiContext 생성 (API 키와 관련된 설정)
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apikey)  // API 키 설정
                .build();

        // FindPlaceFromTextRequest 객체 생성
        FindPlaceFromTextRequest request = new FindPlaceFromTextRequest(context)
                .input(name)  // 입력한 장소 이름과 주소
                .inputType(FindPlaceFromTextRequest.InputType.TEXT_QUERY)  // 입력 형식 (텍스트 쿼리)
                .fields(FindPlaceFromTextRequest.FieldMask.PLACE_ID);   // 반환할 필드 (place_id만 요청)

        // API 호출
        FindPlaceFromText response = request.await();


        // 결과에서 place_id 추출
        if (response.candidates != null && response.candidates.length > 0) {
            System.out.println(response.candidates[0].placeId);
            return response.candidates[0].placeId;
        } else {
            throw new Exception("No place found for the given input.");
        }
    }


    public boolean isPlaceIdFound(String apikey, String name) throws Exception {
        System.out.println(name);
        // GeoApiContext 생성 (API 키와 관련된 설정)
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apikey)  // API 키 설정
                .build();

        // FindPlaceFromTextRequest 객체 생성
        FindPlaceFromTextRequest request = new FindPlaceFromTextRequest(context)
                .input(name)  // 입력한 장소 이름과 주소
                .inputType(FindPlaceFromTextRequest.InputType.TEXT_QUERY)  // 입력 형식 (텍스트 쿼리)
                .fields(FindPlaceFromTextRequest.FieldMask.PLACE_ID);   // 반환할 필드 (place_id만 요청)

        // API 호출
        FindPlaceFromText response = request.await();

        // 결과에서 place_id 추출
        if (response.candidates != null && response.candidates.length > 0) {
            System.out.println(response.candidates[0].placeId);
            return true;  // place_id가 있으면 true 반환
        } else {
            return false;  // place_id가 없으면 false 반환
        }
    }
}