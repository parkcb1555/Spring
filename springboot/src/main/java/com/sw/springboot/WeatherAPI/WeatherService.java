package com.sw.springboot.WeatherAPI;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class WeatherService {
    @Value("${weather.api.key}")
    private String weatherApiKey;

    public String printApiKey() {
        return weatherApiKey;
    }
}
