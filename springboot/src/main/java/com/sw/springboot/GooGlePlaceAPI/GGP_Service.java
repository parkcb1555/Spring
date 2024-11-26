package com.sw.springboot.GooGlePlaceAPI;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GGP_Service {
    private final GeoApiContext context;

    public GGP_Service(@Value("${google.api.key}") String apiKey) {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
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
                .radius(3000) // 1km 반경
                .type(PlaceType.LODGING) // 장소 유형
                .language("ko")
                .await();
    }
}
