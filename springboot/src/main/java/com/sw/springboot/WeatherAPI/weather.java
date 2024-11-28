package com.sw.springboot.WeatherAPI;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class weather {

    //단기 예보
    @GetMapping("/VilageWeather")
    public Map<LocalDate, Double> VilageFcstInfoService(String weahterApiKey,LocalDate StartDate, LocalDate EndDate) throws IOException {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = now.format(formatter);


        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "="+weahterApiKey); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
        urlBuilder.append("&" + URLEncoder.encode("base_date","UTF-8") + "=" + URLEncoder.encode(formattedDate, "UTF-8")); /*‘21년 6월 28일 발표*/
        urlBuilder.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + URLEncoder.encode("0500", "UTF-8")); /*06시 발표(정시단위) */
        urlBuilder.append("&" + URLEncoder.encode("nx","UTF-8") + "=" + URLEncoder.encode("60", "UTF-8")); /*예보지점의 X 좌표값*/
        urlBuilder.append("&" + URLEncoder.encode("ny","UTF-8") + "=" + URLEncoder.encode("120", "UTF-8")); /*예보지점의 Y 좌표값*/

        URL url = new URL(urlBuilder.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());

        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        // JSON 데이터 파싱 및 값 추출
        String jsonResponse = sb.toString();
//        Map<LocalDate, Double> SKYValues = extractFcstValueAveragePerDate(jsonResponse,StartDate,EndDate);



        // JSON 유효성 검사
        if (jsonResponse == null || jsonResponse.isEmpty() || !jsonResponse.trim().startsWith("{")) {
            throw new JSONException("Invalid JSON response: " + jsonResponse);
        }

        // JSON 파싱 및 데이터 처리
        Map<LocalDate, Double> SKYValues;
        try {
            SKYValues = extractFcstValueAveragePerDate(jsonResponse, StartDate,EndDate);
        } catch (JSONException e) {
            throw new RuntimeException("Error parsing JSON response: " + e.getMessage(), e);
        }

        return SKYValues;
    }

    public static Map<LocalDate, Double> extractFcstValueAveragePerDate(String jsonResponse, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<Double>> dateValuesMap = new HashMap<>();
        LocalDate FinalDataDate = startDate;
        FinalDataDate = FinalDataDate.plusDays(2);

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray items = jsonObject.getJSONObject("response")
                    .getJSONObject("body")
                    .getJSONObject("items")
                    .getJSONArray("item");

            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate) || !currentDate.isAfter(FinalDataDate))  {
                LocalDate fcstDate = null;
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);

                    // 날짜 필터링
                    String fcstDateString = item.getString("fcstDate");
                    fcstDate = LocalDate.parse(fcstDateString.substring(0, 4) + "-" + fcstDateString.substring(4, 6) + "-" + fcstDateString.substring(6, 8));

                    if (fcstDate.equals(currentDate) && item.getString("category").equals("SKY")) {
                        double fcstValue = Double.parseDouble(item.getString("fcstValue"));
                        String fcstTime = item.getString("fcstTime");
                        // 날짜별 fcstValue 추가
                        dateValuesMap.computeIfAbsent(currentDate, k -> new ArrayList<>()).add(fcstValue);
                    }
                }

                if (currentDate.equals(FinalDataDate)) {
                    System.out.println("currentDate.equals(FinalDataDate)");
                    // 날짜에 해당하는 데이터가 없으면 함수를 종료
                    break;
                }



                // 날짜 증가
                currentDate = currentDate.plusDays(1);
            }

            // 날짜별 평균값 계산
            Map<LocalDate, Double> dateAvgMap = new HashMap<>();
            for (Map.Entry<LocalDate, List<Double>> entry : dateValuesMap.entrySet()) {
                List<Double> values = entry.getValue();
                double sum = 0;
                for (Double value : values) {
                    sum += value;
                }
                double average = Math.round((sum / values.size()) * 100.0) / 100.0;
                dateAvgMap.put(entry.getKey(), average);
            }
            return dateAvgMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    @GetMapping("/MidWeather")
    //중기 예보
    public String MidFcstInfoService(String weahterApiKey,LocalDate StartDate, LocalDate EndDate) throws IOException {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = now.format(formatter);

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "="+weahterApiKey); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON)Default: XML*/
        urlBuilder.append("&" + URLEncoder.encode("regId","UTF-8") + "=" + URLEncoder.encode("11B00000", "UTF-8")); /*11B0000 서울, 인천, 경기도 11D10000 등 (활용가이드 하단 참고자료 참조)*/
        urlBuilder.append("&" + URLEncoder.encode("tmFc","UTF-8") + "=" + URLEncoder.encode(formattedDate+"0600", "UTF-8")); /*-일 2회(06:00,18:00)회 생성 되며 발표시각을 입력 YYYYMMDD0600(1800)-최근 24시간 자료만 제공*/

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());

        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();


        return sb.toString();
    }
}
