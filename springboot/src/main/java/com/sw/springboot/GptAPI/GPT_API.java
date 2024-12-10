package com.sw.springboot.GptAPI;


import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GPT_API {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final int BUFFER_SIZE = 4096;
    private static final int MAX_TOKENS = 800; // 최대 토큰 수 설정
    String model = "gpt-4o-mini"; // 모델을 gpt-4o-mini

    @Autowired
    CacheService cacheService;

    //장소 주소,운영시간,가격티어 구하기
    public String Gpt_Request(String apikey,String latitude,String longitude,String name, String types) {

//        CacheService cacheService = new CacheService();

        // 먼저 캐시에서 데이터 확인
        Optional<Cache> existingCache = cacheService.getCacheByNameAndTypes(name, types);
        String text = "";


        // 캐시가 존재하면 캐시에서 응답 반환
        if (existingCache.isPresent()) {
            System.out.println("Cache hit: " + name + " " + types);
            text = existingCache.get().getResponse();
        }
        else {
            try {
                String prompt = "";
                if (types.equals("address")) { //주소
                    prompt = "위도가 " + latitude + "이고 경도가 " + longitude + "인 " + name + "의 주소를 '???의 주소는 ???입니다.' 라는 형식으로 출력해줘";
                } else if (types.equals("regularHours")) { // 운영시간
                    prompt = "위도가 " + latitude + "이고 경도가 " + longitude + "인 " + name + "의 모든 요일의 운영 시간을  '\n" + "'{\"regular\":[{\"close\":\"????\",\"day\":1,\"open\":\"????\"},{\"close\":\"????\",\"day\":2,\"open\":\"????\"},{\"close\":\"????\",\"day\":3,\"open\":\"????\"},{\"close\":\"????\",\"day\":4,\"open\":\"????\"},{\"close\":\"????\",\"day\":5,\"open\":\"????\"},{\"close\":\"????\",\"day\":6,\"open\":\"????\"},{\"close\":\"????\",\"day\":7,\"open\":\"????\"}]}'" +
                            "'의 JSON 형식으로 알려줘.\n" +
                            "월요일은 1, 화요일은 2, 수요일은 3, 목요일은 4, 금요일은 5, 토요일은 6, 일요일은 7로 표현하고\n" +
                            "반드시 close이 첫번째, day가 2번째, open가 3번째 순이어야 하고" +
                            "24시간 개방이면 \"open\": \"0000\", \"close\": \"2400\"으로 표현해. 만약 운영시간에 대해 찾을 수 없으면 9시부터 18시인걸로 해줘";
                } else if (types.equals("priceTier")) {
                    prompt = "위도가 " + latitude + "이고 경도가 " + longitude + "인 " + name + " 입장 비용을 0=무료, 1 = 저렴, 2 = 보통, 3 = 비쌈, 4 = 매우 비쌈을 기준으로 숫자만 표현해서 알려줘";
                } else if (types.equals("RestaurantpriceTier")) {
                    prompt = "위도가 " + latitude + "이고 경도가 " + longitude + "인 " + name + " 에서 식사 비용을 0=무료, 1 = 저렴, 2 = 보통, 3 = 비쌈, 4 = 매우 비쌈을 기준으로 숫자만 표현해서 알려줘. 만약 알수없는 정보이면 0으로 표현해";
                    System.out.println(prompt);
                }else if (types.equals("Restaurantprice")) { //식사 비용
                    prompt = name + "에서 식사를 하게 되면, 평균적인 1인 식사 금액은 얼마인가요? 금액은 '???' 원 입니다. 라는 형식으로 출력해 주세요. 가능하면 가격을 예상해서 출력해 주세요.";
                    System.out.println(prompt);
                }else if (types.equals("Spotprice")) { //여행지 소모 금액
                    prompt = name + "에서 소모되는 성인 1명의 입장금액은 얼마야? 금액은 '???'원 입니다. 라는 형식으로 출력해줘. 무료이면 0원으로";
                    System.out.println(prompt);
                }else if (types.equals("HotelStar")) { //호텔 등급
                    prompt = name + "의 호텔 등급은?  등급은 '??성' 입니다. 라는 형식으로 출력해";
                    System.out.println(prompt);
                }




                String url = "https://api.openai.com/v1/chat/completions";
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", model);
                requestBody.put("max_tokens", MAX_TOKENS);
                JSONArray messages = new JSONArray();
                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", prompt);
                messages.put(message);
                requestBody.put("messages", messages);


                // temperature 설정
                requestBody.put("temperature", 0.4); // 온도 설정

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apikey); // apikey 확인
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                text = parseAndPrintResponse(apikey, response.toString(), types);

                if (text.equals("")) {
                    text = "0";
                }

                cacheService.saveCache(name,types,text);

            } catch (Exception e) {
                System.err.println("API 호출 중 오류 발생: " + e.getMessage());
            }
        }
        return text;
    }

    private static String parseAndPrintResponse(String apikey,String responseBody, String types) throws JSONException {
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray choices = jsonObject.getJSONArray("choices");
        String extracttext = "";

        for (int i = 0; i < choices.length(); i++) {
            String text = choices.getJSONObject(i).getJSONObject("message").getString("content");
            if(types.equals("address")){
                extracttext = extractAddress(text);
            } else if (types.equals("regularHours")) {
                extracttext= extractregularHours(text);
            } else if (types.equals("priceTier")) {
                extracttext =extractNumber(text);
            } else if (types.equals("HotelStar")) {
                extracttext = extractHotelGrade(text);
                System.out.println(text);
                System.out.println(extracttext);
            } else if (types.equals("Restaurantprice") || types.equals("Spotprice")) {
                extracttext = extractPrice(text);
                System.out.println(text);
                System.out.println(extracttext);
            }


            // Check for continuation token and fetch next response if available
            if (jsonObject.has("next")) {
                fetchNextResponse(apikey,jsonObject.getString("next"),types);
            }
        }
        return extracttext;
    }
    private static void fetchNextResponse(String apikey,String nextToken,String types) {
        // Implementation for fetching next response using the continuation token
        try {
            // API URL 설정
            String url = "https://api.openai.com/v1/chat/completions";

            // JSON 요청 본문 생성
            JSONObject requestBody = new JSONObject();
            requestBody.put("next", nextToken);

            // HTTP 연결 설정
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apikey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 요청 본문 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 응답 받기
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // 응답 파싱 및 출력
            parseAndPrintResponse(apikey,response.toString(),types);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String extractAddress(String input) {
        // 주소가 시작되는 부분과 끝나는 부분을 찾아서 추출
        String[] parts = input.split("주소는 "); // "주소는 "를 기준으로 나누기
        if (parts.length > 1) {
            String addressPart = parts[1].replace("입니다.", "").trim(); // "입니다." 제거 및 공백 제거
            return addressPart;
        }
        return "주소가 없습니다.";
    }
    public static String extractHotelGrade(String input) {
        // 정규 표현식을 이용해 '5성' 부분만 추출
        Pattern pattern = Pattern.compile("'([^']+)'");
        Matcher matcher = pattern.matcher(input);
        String hotelGrade = "";

        if (matcher.find()) {
            // 추출한 등급 부분
            hotelGrade = matcher.group(1);

            // 결과 출력
            System.out.println("호텔 등급: " + hotelGrade);  // 출력: 호텔 등급: 5성
        }
        return hotelGrade;
    }

    public static String extractPrice(String input) {
        // 주소가 시작되는 부분과 끝나는 부분을 찾아서 추출
        // 정규 표현식을 이용해 숫자 부분만 추출
        Pattern pattern = Pattern.compile("'([\\d,]+)'");
        Matcher matcher = pattern.matcher(input);
        String costWithoutComma = "";
        if (matcher.find()) {
            // 추출한 숫자 부분 (쉼표 포함)
            String costWithComma = matcher.group(1);

            // 쉼표 제거
            costWithoutComma = costWithComma.replace(",", "");

            // 결과 출력
            System.out.println("소모된 비용: " + costWithoutComma); // 출력: 소모된 비용: 20000
        }
        return costWithoutComma;
    }

    public static String extractNumber(String input) {
        Pattern pattern = Pattern.compile("\\d"); // 0부터 9까지의 숫자
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            result.append(matcher.group()); // 찾은 숫자 추가
        }

        return result.length() > 0 ? result.toString() : "숫자가 없습니다.";
    }

    public static String extractregularHours(String input) {
        // 정규 표현식 패턴
        String pattern = "\"regular\": \\[(.*?)\\]";
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher matcher = compiledPattern.matcher(input);
        String regularHours = "";
        // 매칭된 부분 추출
        if (matcher.find()) {
            regularHours = matcher.group(0); // 전체 매칭된 부분
//            System.out.println(regularHours); // 출력
        } else {
            System.out.println("운영 시간 정보를 찾을 수 없습니다.");
        }
        return regularHours;
    }

    // 평균 관람 시간 받아옴
    public static int AverageTime(String apikey,String latitude,String longitude,String place){
        ////========================GPT API function 호출=================================================
        String url = "https://api.openai.com/v1/chat/completions";
//		String place = "경복궁";
        String authorizationKey = "Bearer "+apikey; // 여기에 자신의 API 키 입력
        int averageVisitTime = 0;
        String jsonInputString = "{"
                + "\"model\": \"gpt-4o-mini\","
                + "\"messages\": ["
                + "  {"
                + "    \"role\": \"user\","
                + "    \"content\": \""+"위도가 "+latitude+"이고 경도가 "+longitude+"인 "+place + " 평균 관람 시간을 분단위로 알려줘\""
                + "  }"
                + "],"
                + "\"tools\": ["
                + "  {"
                + "    \"type\": \"function\","
                + "    \"function\": {"
                + "      \"name\": \"myFunction\","
                + "      \"description\": \"평균 관람 시간 함수\","
                + "      \"parameters\": {"
                + "        \"type\": \"object\","
                + "        \"properties\": {"
                + "          \"averageVisitTime\": {"
                + "            \"type\": \"number\""
                + "          }"
                + "        },"
                + "        \"required\": [\"averageVisitTime\"]"
                + "      }"
                + "    }"
                + "  }"
                + "],"
                + "\"tool_choice\": {"
                + "  \"type\": \"function\","
                + "  \"function\": {"
                + "    \"name\": \"myFunction\""
                + "  }"
                + "},"
                + "\"temperature\": 0.4"
                + "}";

        HttpURLConnection con = null;
        try {
            // URL 객체 생성
            URL urlObject = new URL(url);
            con = (HttpURLConnection) urlObject.openConnection();

            // 요청 설정
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", authorizationKey);
            con.setDoOutput(true);

            // 요청 본문에 JSON 데이터 추가
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 응답 코드 확인
//			int responseCode = con.getResponseCode();
//			System.out.println("Response Code : " + responseCode);

            // 응답 읽기
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 응답 JSON 출력 (디버깅)
//			System.out.println("Response JSON: " + response.toString());

            // 응답 JSON 파싱
            JSONObject jsonResponse = new JSONObject(response.toString());

            // "choices" 배열에서 첫 번째 선택지의 tool_calls 확인
            JSONArray choices = jsonResponse.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");

            if (message.has("tool_calls")) {
                JSONArray toolCalls = message.getJSONArray("tool_calls");
                JSONObject firstToolCall = toolCalls.getJSONObject(0);
                JSONObject function = firstToolCall.getJSONObject("function");

                // arguments는 문자열로 저장되어 있으므로 다시 파싱
                String argumentsString = function.getString("arguments");
                JSONObject argumentsJson = new JSONObject(argumentsString);

                // averageVisitTime 값 추출
                if (argumentsJson.has("averageVisitTime")) {
                    averageVisitTime = argumentsJson.getInt("averageVisitTime");
//                    System.out.println("평균 관람 시간: " + averageVisitTime+"분");
                } else {
                    System.out.println("No value for 'averageVisitTime'");
                }
            } else {
                System.out.println("No 'tool_calls' found in the response.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return averageVisitTime;
    }

    // 관광지 추천 이유 받아옴
    public String PlaceChooseReason(String apikey,String latitude,String longitude,String place){
        ////========================GPT API function 호출=================================================
        String url = "https://api.openai.com/v1/chat/completions";
        String authorizationKey = "Bearer "+apikey; // 여기에 자신의 API 키 입력
        String jsonInputString = "{"
                + "\"model\": \"gpt-4o-mini\","
                + "\"messages\": ["
                + "  {"
                + "    \"role\": \"user\","
                + "    \"content\": \"" +"위도가 "+latitude+"이고 경도가 "+longitude+"인 "+ place + "의 여행지 추천 이유를 70글자 이내로 알려줘\""
                + "  }"
                + "],"
                + "\"tools\": ["
                + "  {"
                + "    \"type\": \"function\","
                + "    \"function\": {"
                + "      \"name\": \"myFunction\","
                + "      \"description\": \"장소 추천 이유 함수\","
                + "      \"parameters\": {"
                + "        \"type\": \"object\","
                + "        \"properties\": {"
                + "          \"answer\": {"
                + "            \"type\": \"string\""
                + "          }"
                + "        },"
                + "        \"required\": [\"answer\"]"
                + "      }"
                + "    }"
                + "  }"
                + "],"
                + "\"tool_choice\": {"
                + "  \"type\": \"function\","
                + "  \"function\": {"
                + "    \"name\": \"myFunction\""
                + "  }"
                + "},"
                + "\"temperature\": 0.4"
                + "}";

        HttpURLConnection con = null;
        String answer ="";

        // 먼저 캐시에서 데이터 확인
        Optional<Cache> existingCache = cacheService.getCacheByNameAndTypes(place, "ChooseReason");


        // 캐시가 존재하면 캐시에서 응답 반환
        if (existingCache.isPresent()) {
            System.out.println("Cache hit: " + place + " " + "ChooseReason");
            answer = existingCache.get().getResponse();
        }
        else {

            try {
                // URL 객체 생성
                URL urlObject = new URL(url);
                con = (HttpURLConnection) urlObject.openConnection();

                // 요청 설정
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", authorizationKey);
                con.setDoOutput(true);

                // 요청 본문에 JSON 데이터 추가
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // 응답 코드 확인
//			int responseCode = con.getResponseCode();
//			System.out.println("Response Code : " + responseCode);

                // 응답 읽기
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 응답 JSON 출력 (디버깅)
//			System.out.println("Response JSON: " + response.toString());

                // 응답 JSON 파싱
                JSONObject jsonResponse = new JSONObject(response.toString());

                // "choices" 배열에서 첫 번째 선택지의 tool_calls 확인
                JSONArray choices = jsonResponse.getJSONArray("choices");
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");

                if (message.has("tool_calls")) {
                    JSONArray toolCalls = message.getJSONArray("tool_calls");
                    JSONObject firstToolCall = toolCalls.getJSONObject(0);
                    JSONObject function = firstToolCall.getJSONObject("function");

                    // arguments는 문자열로 저장되어 있으므로 다시 파싱
                    String argumentsString = function.getString("arguments");
                    JSONObject argumentsJson = new JSONObject(argumentsString);

                    //answer 값 추출
                    if (argumentsJson.has("answer")) {
                        answer = argumentsJson.getString("answer");
                        cacheService.saveCache(place,"ChooseReason",answer);
                    } else {
                        System.out.println("No value for 'answer'");
                    }
                } else {
                    System.out.println("No 'tool_calls' found in the response.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }

        return answer;
    }
}




////========================GPT API function 호출=================================================
//		String url = "https://api.openai.com/v1/chat/completions";
//		String place = "경복궁";
//		String authorizationKey = "Bearer "+API_KEY; // 여기에 자신의 API 키 입력
//		String jsonInputString = "{"
//				+ "\"model\": \"gpt-4o-mini\","
//				+ "\"messages\": ["
//				+ "  {"
//				+ "    \"role\": \"user\","
//				+ "    \"content\": \"" + place + " 평균 관람 시간을 분단위로 알려줘\""
//				+ "  }"
//				+ "],"
//				+ "\"tools\": ["
//				+ "  {"
//				+ "    \"type\": \"function\","
//				+ "    \"function\": {"
//				+ "      \"name\": \"myFunction\","
//				+ "      \"description\": \"함수의 동작 정의\","
//				+ "      \"parameters\": {"
//				+ "        \"type\": \"object\","
//				+ "        \"properties\": {"
//				+ "          \"averageVisitTime\": {"
//				+ "            \"type\": \"number\""
//				+ "          }"
//				+ "        },"
//				+ "        \"required\": [\"averageVisitTime\"]"
//				+ "      }"
//				+ "    }"
//				+ "  }"
//				+ "],"
//				+ "\"tool_choice\": {"
//				+ "  \"type\": \"function\","
//				+ "  \"function\": {"
//				+ "    \"name\": \"myFunction\""
//				+ "  }"
//				+ "},"
//				+ "\"temperature\": 0.4"
//				+ "}";
//
//		HttpURLConnection con = null;
//		try {
//			// URL 객체 생성
//			URL urlObject = new URL(url);
//			con = (HttpURLConnection) urlObject.openConnection();
//
//			// 요청 설정
//			con.setRequestMethod("POST");
//			con.setRequestProperty("Content-Type", "application/json");
//			con.setRequestProperty("Authorization", authorizationKey);
//			con.setDoOutput(true);
//
//			// 요청 본문에 JSON 데이터 추가
//			try (OutputStream os = con.getOutputStream()) {
//				byte[] input = jsonInputString.getBytes("utf-8");
//				os.write(input, 0, input.length);
//			}
//
//			// 응답 코드 확인
//			int responseCode = con.getResponseCode();
//			System.out.println("Response Code : " + responseCode);
//
//			// 응답 읽기
//			BufferedReader in = new BufferedReader(
//					new InputStreamReader(con.getInputStream(), "utf-8"));
//			String inputLine;
//			StringBuffer response = new StringBuffer();
//			while ((inputLine = in.readLine()) != null) {
//				response.append(inputLine);
//			}
//			in.close();
//
//			// 응답 JSON 출력 (디버깅)
//			System.out.println("Response JSON: " + response.toString());
//
//			// 응답 JSON 파싱
//			JSONObject jsonResponse = new JSONObject(response.toString());
//
//			// "choices" 배열에서 첫 번째 선택지의 tool_calls 확인
//			JSONArray choices = jsonResponse.getJSONArray("choices");
//			JSONObject firstChoice = choices.getJSONObject(0);
//			JSONObject message = firstChoice.getJSONObject("message");
//
//			if (message.has("tool_calls")) {
//				JSONArray toolCalls = message.getJSONArray("tool_calls");
//				JSONObject firstToolCall = toolCalls.getJSONObject(0);
//				JSONObject function = firstToolCall.getJSONObject("function");
//
//				// arguments는 문자열로 저장되어 있으므로 다시 파싱
//				String argumentsString = function.getString("arguments");
//				JSONObject argumentsJson = new JSONObject(argumentsString);
//
//				// averageVisitTime 값 추출
//				if (argumentsJson.has("averageVisitTime")) {
//					int averageVisitTime = argumentsJson.getInt("averageVisitTime");
//					System.out.println("Average Visit Time: " + averageVisitTime);
//				} else {
//					System.out.println("No value for 'averageVisitTime'");
//				}
//			} else {
//				System.out.println("No 'tool_calls' found in the response.");
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (con != null) {
//				con.disconnect();
//			}
//		}