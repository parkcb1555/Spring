package com.sw.springboot.GooGlePlaceAPI;

import com.google.maps.GeoApiContext;
import com.google.maps.DirectionsApi;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

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

    public DirectionsResult getDirections(double originLat, double originLng, double destinationLat, double destinationLng)  throws Exception {

        return DirectionsApi.newRequest(context)
                .mode(TravelMode.TRANSIT) // 운전 모드, 필요에 따라 변경 가능
                .origin(new LatLng(originLat,originLng))
                .destination(new LatLng(destinationLat,destinationLng))
//                .alternatives(true)
                .language("ko")
                .await();
    }
}