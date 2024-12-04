package com.sw.springboot.FoursquareAPI;

import com.google.maps.GeoApiContext;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Getter
@Service
public class FoursquareService {

    @Value("${foursquare.api.key}")
    private String foursquareApiKey;

    public String printApiKey() {

        return foursquareApiKey;
    }
}