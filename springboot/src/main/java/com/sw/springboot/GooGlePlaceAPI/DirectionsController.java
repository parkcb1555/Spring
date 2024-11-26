package com.sw.springboot.GooGlePlaceAPI;

import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
//
//
//@RestController
//public class DirectionsController {
//    private final DirectionsService directionsService;
//
//    public DirectionsController(DirectionsService directionsService) {
//        this.directionsService = directionsService;
//    }
//
//    @GetMapping("/directions")
//    public ResponseEntity<DirectionsResult> getDirections(
//            @RequestParam String origin,
//            @RequestParam String destination,
//            @RequestParam(required = false) List<String> waypoints) {
//        try {
//            DirectionsResult directions = directionsService.getDirections(origin, destination, waypoints);
//            return ResponseEntity.ok(directions);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//}

@RestController
public class DirectionsController {
    private final DirectionsService directionsService;

    public DirectionsController(DirectionsService directionsService) {
        this.directionsService = directionsService;
    }

    @GetMapping("/directions")
    public DirectionsResult getDirections(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destinationLat,
            @RequestParam double destinationLng) throws Exception {
        return directionsService.getDirections(originLat, originLng,destinationLat,destinationLng);
    }

}