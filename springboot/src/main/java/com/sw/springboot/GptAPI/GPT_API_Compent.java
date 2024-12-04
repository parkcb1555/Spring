package com.sw.springboot.GptAPI;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Getter
@Service
public class GPT_API_Compent {

    String apiKey;

    @Value("${gpt.api.key}")
    private String gptApiKey;


    public GPT_API_Compent( @Value("${gpt.api.key}") String key){
        this.apiKey = key;
    }


    public String printApiKey() {
        return gptApiKey;
    }
}
