package com.sw.springboot.GooGlePlaceAPI;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.model.*;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GGP_Controller {
    public final GGP_Service googlePlaceService;

    @Autowired
    public GGP_Controller(GGP_Service googlePlaceService) {
        this.googlePlaceService = googlePlaceService;
    }

    @GetMapping("/places")
    public PlacesSearchResponse searchPlaces(
            @RequestParam String query,
            @RequestParam String lat,
            @RequestParam String lng) throws Exception {

        searchPlacesDetail("GGP_Controller : "+googlePlaceService.printApiKey());
        return googlePlaceService.searchPlaces(query, lat, lng);
    }

    @GetMapping("/placedetail")
    public PlaceDetails searchPlacesDetail(
            @RequestParam String placeId) throws Exception {
        return googlePlaceService.searchPlacesDetail(placeId);
    }


    @GetMapping("/nearby-places")
    public PlacesSearchResponse getNearbyPlaces(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam String type) throws Exception {

        return googlePlaceService.searchNearbyPlaces(lat, lng, type);
    }

    public LatLng getCoordinates(String address) throws Exception {
        // 도로명 주소로 위도, 경도를 구합니다.
        return googlePlaceService.getLatLngFromAddress(address);
    }
}
