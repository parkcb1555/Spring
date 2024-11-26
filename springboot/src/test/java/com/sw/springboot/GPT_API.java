package com.sw.springboot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPT_API {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final int BUFFER_SIZE = 4096;
    private static final int MAX_TOKENS = 4096; // 최대 토큰 수 설정
    String model = "gpt-4o-mini"; // 모델을 gpt-4o-mini로 수정


    String Gpt_Request(String name, String types) {
        String text = "";
        try {
            String prompt = "";
            if(types.equals("address")){
                prompt = name+"의 주소 정보만 '???의 주소는 ???입니다.' 라는 형식으로 알려줘";
            } else if (types.equals("regularHours")) {
                prompt = name + "의 모든 요일의 운영 시간을  '\n" +
                        "{\n" +
                        "  \"regular\": [\n" +
                        "    {\n" +
                        "      \"close\": \"????\",\n" +
                        "      \"day\": 1,\n" + // 월요일
                        "      \"open\": \"????\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"close\": \"????\",\n" +
                        "      \"day\": 2,\n" + // 화요일
                        "      \"open\": \"????\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"close\": \"????\",\n" +
                        "      \"day\": 3,\n" + // 수요일
                        "      \"open\": \"????\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"close\": \"????\",\n" +
                        "      \"day\": 4,\n" + // 목요일
                        "      \"open\": \"????\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"close\": \"????\",\n" +
                        "      \"day\": 5,\n" + // 금요일
                        "      \"open\": \"????\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"close\": \"????\",\n" +
                        "      \"day\": 6,\n" + // 토요일
                        "      \"open\": \"????\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"close\": \"????\",\n" +
                        "      \"day\": 7,\n" + // 일요일
                        "      \"open\": \"????\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n" +
                        "'의 JSON 형식으로 알려줘.\n" +
                        "월요일은 1, 화요일은 2, 수요일은 3, 목요일은 4, 금요일은 5, 토요일은 6, 일요일은 7로 표현하고\n" +
                        "24시간 개방이면 \"open\": \"0000\", \"close\": \"2400\"으로 표현해.";
            } else if (types.equals("priceTier")) {
                prompt = name+" 입장 비용을 0=무료, 1 = 저렴, 2 = 보통, 3 = 비싸, 4 = 매우 비쌈을 기준으로 숫자만 표현해서 알려줘";
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
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
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

            text = parseAndPrintResponse(response.toString(),types);

        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
        }
        return text;
    }

    private static String parseAndPrintResponse(String responseBody, String types) throws JSONException {
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
        }


        // Check for continuation token and fetch next response if available
        if (jsonObject.has("next")) {
            fetchNextResponse(jsonObject.getString("next"),types);
        }
    }
    return extracttext;
}
	private static void fetchNextResponse(String nextToken,String types) {
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
			connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
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
			parseAndPrintResponse(response.toString(),types);

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


// 평균 관람 시간 받아옴
//private static void gpt_time(String place){
//    ////========================GPT API function 호출=================================================
//    String url = "https://api.openai.com/v1/chat/completions";
////		String place = "경복궁";
//    String authorizationKey = "Bearer "+API_KEY; // 여기에 자신의 API 키 입력
//    String jsonInputString = "{"
//            + "\"model\": \"gpt-4o-mini\","
//            + "\"messages\": ["
//            + "  {"
//            + "    \"role\": \"user\","
//            + "    \"content\": \"" + place + " 평균 관람 시간을 분단위로 알려줘\""
//            + "  }"
//            + "],"
//            + "\"tools\": ["
//            + "  {"
//            + "    \"type\": \"function\","
//            + "    \"function\": {"
//            + "      \"name\": \"myFunction\","
//            + "      \"description\": \"함수의 동작 정의\","
//            + "      \"parameters\": {"
//            + "        \"type\": \"object\","
//            + "        \"properties\": {"
//            + "          \"averageVisitTime\": {"
//            + "            \"type\": \"number\""
//            + "          }"
//            + "        },"
//            + "        \"required\": [\"averageVisitTime\"]"
//            + "      }"
//            + "    }"
//            + "  }"
//            + "],"
//            + "\"tool_choice\": {"
//            + "  \"type\": \"function\","
//            + "  \"function\": {"
//            + "    \"name\": \"myFunction\""
//            + "  }"
//            + "},"
//            + "\"temperature\": 0.4"
//            + "}";
//
//    HttpURLConnection con = null;
//    try {
//        // URL 객체 생성
//        URL urlObject = new URL(url);
//        con = (HttpURLConnection) urlObject.openConnection();
//
//        // 요청 설정
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Content-Type", "application/json");
//        con.setRequestProperty("Authorization", authorizationKey);
//        con.setDoOutput(true);
//
//        // 요청 본문에 JSON 데이터 추가
//        try (OutputStream os = con.getOutputStream()) {
//            byte[] input = jsonInputString.getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
//
//        // 응답 코드 확인
////			int responseCode = con.getResponseCode();
////			System.out.println("Response Code : " + responseCode);
//
//        // 응답 읽기
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(con.getInputStream(), "utf-8"));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
//
//        // 응답 JSON 출력 (디버깅)
////			System.out.println("Response JSON: " + response.toString());
//
//        // 응답 JSON 파싱
//        JSONObject jsonResponse = new JSONObject(response.toString());
//
//        // "choices" 배열에서 첫 번째 선택지의 tool_calls 확인
//        JSONArray choices = jsonResponse.getJSONArray("choices");
//        JSONObject firstChoice = choices.getJSONObject(0);
//        JSONObject message = firstChoice.getJSONObject("message");
//
//        if (message.has("tool_calls")) {
//            JSONArray toolCalls = message.getJSONArray("tool_calls");
//            JSONObject firstToolCall = toolCalls.getJSONObject(0);
//            JSONObject function = firstToolCall.getJSONObject("function");
//
//            // arguments는 문자열로 저장되어 있으므로 다시 파싱
//            String argumentsString = function.getString("arguments");
//            JSONObject argumentsJson = new JSONObject(argumentsString);
//
//            // averageVisitTime 값 추출
//            if (argumentsJson.has("averageVisitTime")) {
//                int averageVisitTime = argumentsJson.getInt("averageVisitTime");
//                System.out.println("평균 관람 시간: " + averageVisitTime+"분");
//            } else {
//                System.out.println("No value for 'averageVisitTime'");
//            }
//        } else {
//            System.out.println("No 'tool_calls' found in the response.");
//        }
//
//    } catch (Exception e) {
//        e.printStackTrace();
//    } finally {
//        if (con != null) {
//            con.disconnect();
//        }
//    }
//}