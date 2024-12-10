package com.sw.springboot.GooGlePlaceAPI;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Service
public class GGP_Service {

//    @Value("${google.api.key}")
    String key;

    private final GeoApiContext context;

    public String printApiKey() {

        return key;
    }

    // 생성자 주입
    public GGP_Service(@Value("${google.api.key}") String apikey) {
        this.key = apikey;
        this.context = new GeoApiContext.Builder()
                .apiKey(key)
                .build();
    }

    public PlacesSearchResponse searchPlaces(String query, String lat, String lng) throws Exception {
        LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        return PlacesApi.textSearchQuery(context, query)
                .location(location)
                .radius(1000) // 1km 범위 내 검색
                .language("ko")
                .await();
    }

    public PlaceDetails searchPlacesDetail(String placeId) throws Exception {
        return PlacesApi.placeDetails(context,placeId).language("ko").await();
    }


    public PlacesSearchResponse searchNearbyPlaces(double lat, double lng, String type) throws Exception {
        // 위치 좌표를 LatLng 객체로 변환
        LatLng location = new LatLng(lat,lng);
        // 근처 검색 쿼리
        return PlacesApi.nearbySearchQuery(context, location)
                .radius(3000) // 3km 반경
                .type(PlaceType.LODGING) // 장소 유형
                .language("ko")
                .await();
    }

    public PlacesSearchResponse getHotelByAddress(String address) throws IOException, InterruptedException, ApiException {
        GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
        LatLng location = new LatLng(results[0].geometry.location.lat,results[0].geometry.location.lng);
        System.out.println(address+"     "+results[0].geometry.location.lat+"      "+results[0].geometry.location.lng);
        return PlacesApi.nearbySearchQuery(context, location)
                .radius(100) // 500m 반경
                .type(PlaceType.LODGING) // 장소 유형
                .language("ko")
                .await();
    }


    // 페이지 순회 검색 메서드 추가
    public PlacesSearchResponse searchNearbyPlacesWithPagination(double lat, double lng, String type, String hotelTag) throws Exception {
        LatLng location = new LatLng(lat, lng);
        PlacesSearchResponse response = PlacesApi.nearbySearchQuery(context, location)
                .radius(3000)
                .type(PlaceType.valueOf(type.toUpperCase()))
                .language("ko")
                .await();

        // 첫 페이지 결과 확인
        if (findMatchingHotel(response, hotelTag)) {
            return response;
        }

        // 다음 페이지가 있는 경우 순회
        while (response.nextPageToken != null) {
            Thread.sleep(2000);  // Google API 지연 대기
            response = PlacesApi.nearbySearchNextPage(context, response.nextPageToken)
                    .language("ko")
                    .await();

            if (findMatchingHotel(response, hotelTag)) {
                return response;
            }
        }

        // 결과가 없으면 빈 응답 반환
        return new PlacesSearchResponse();
    }

    // 호텔 태그 확인 메서드 추가
    private boolean findMatchingHotel(PlacesSearchResponse response, String hotelTag) {
        for (PlacesSearchResult result : response.results) {
            if (hotelTag.equalsIgnoreCase(result.name)) {
                System.out.println("일치하는 호텔: " + result.name);
                return true;
            }
        }
        return false;
    }
}
