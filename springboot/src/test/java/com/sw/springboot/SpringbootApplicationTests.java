package com.sw.springboot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;



import java.io.IOException;
import okhttp3.*;
import okhttp3.OkHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.google.gson.Gson;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.sw.springboot.GPT_API;

import com.sw.springboot.WeatherAPI.weather;
import com.sw.springboot.GooGlePlaceAPI.*;

@SpringBootTest
class SpringbootApplicationTests {
	// OpenAI API 키를 여기에 입력하세요
	private static final String API_KEY = "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";
	private static final String API_URL = "https://api.openai.com/v1/chat/completions";
	private static final int BUFFER_SIZE = 4096;
	private static final int MAX_TOKENS = 4096; // 최대 토큰 수 설정
	private static final String GooGleMap_API_KEY = "AIzaSyBpYHuPzcz63P3hz-xSUNm75qIYmMur9sc"; // 여기에 API 키를 입력하세요

	@Test
	void contextLoads() throws IOException, InterruptedException, JSONException {
		String reason  = gpt_Reason("경복궁");
		System.out.println(reason );

		JsonObject hours = new JsonObject();
		hours.addProperty("Reason",reason);
		System.out.println(hours);
//////////========================Foursquare API 호출 출력 확인용=================================================
//		OkHttpClient client = new OkHttpClient();
//
//		double originLat=0.0;
//		double originLng=0.0;
//		double destinationLat=0.0;
//		double destinationLng=0.0;
//
//		// Foursquare Place Search API 요청 (인기도 순 정렬 및 카테고리 ID 포함)
//		Request request = new Request.Builder()
////				.url("https://api.foursquare.com/v3/places/search?categories=16011,16020,16026,16031&sort=RATING&limit=2&near=서울&fields=name,fsq_id,location,categories,geocodes,hours,price,popularity,rating,stats,tastes")
//				.url("https://api.foursquare.com/v3/places/search?ll=37.5112294847,127.0979394903&categories=13000&sort=RATING&limit=10&fields=name,fsq_id,location,categories,geocodes,hours,price,popularity,rating,stats,tastes")
//
//				.get()
//				.addHeader("Accept", "application/json")
//				.addHeader("Authorization", "fsq3uJb5W14pqg1wFsyGryVzpaJeut5pRRfy0PnZunTmhRI=")  // 발급받은 API 키 사용
//				.build();
//
//		// API 응답 받기
//		Response response = client.newCall(request).execute();
//		String jsonData = response.body().string();
//
//
//		// 응답 데이터를 JSON으로 파싱
//		JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
//		JsonArray resultsArray = jsonObject.getAsJsonArray("results");
//
//
//		// JsonArray -> List<JsonObject> 변환
//		List<JsonObject> placesList = new ArrayList<>();
//		for (int i = 0; i < resultsArray.size(); i++) {
//			placesList.add(resultsArray.get(i).getAsJsonObject());
//		}
//
//
//		//stats가 null인 것들은 이름 순으로
//		placesList.sort((place1, place2) -> {
//			JsonObject stats1 = place1.getAsJsonObject("stats");
//			JsonObject stats2 = place2.getAsJsonObject("stats");
//
//			// stats1과 stats2가 null인 경우 처리
//			if (stats1 == null && stats2 == null) {
//				// 두 개가 모두 null일 경우 이름을 비교하여 정렬
//				return place1.get("name").getAsString().compareTo(place2.get("name").getAsString());
//			} else if (stats1 == null) {
//				// stats1이 null인 경우 stats2 기준으로 이름 정렬
//				return place1.get("name").getAsString().compareTo(place2.get("name").getAsString());
//			} else if (stats2 == null) {
//				// stats2가 null인 경우 stats1 기준으로 이름 정렬
//				return place1.get("name").getAsString().compareTo(place2.get("name").getAsString());
//			}
//
//			// stats가 null이 아닐 경우 리뷰 수와 좋아요 수 가져오기
//			int reviewCount1 = stats1.has("total_ratings") ? stats1.get("total_ratings").getAsInt() : 0;
//			int tips1 = stats1.has("total_tips") ? stats1.get("total_tips").getAsInt() : 0;
//			int reviewCount2 = stats2.has("total_ratings") ? stats2.get("total_ratings").getAsInt() : 0;
//			int tips2 = stats2.has("total_tips") ? stats2.get("total_tips").getAsInt() : 0;
//
//			// 리뷰 수와 좋아요 수의 합 계산
//			int total1 = reviewCount1 + tips1;
//			int total2 = reviewCount2 + tips2;
//
//			// 합이 높은 순으로 정렬 (내림차순)
//			return Integer.compare(total2, total1);
//		});
//
//// 다시 List<JsonObject> -> JsonArray 변환
//		JsonArray sortedResultsArray = new JsonArray();
//		for (JsonObject place : placesList) {
//			sortedResultsArray.add(place);
//		}
//
//		for (int i = 0; i < sortedResultsArray.size(); i++) {
//			JsonObject place = sortedResultsArray.get(i).getAsJsonObject();
////			System.out.println(place);
//		}
//
//
//		// 정렬된 결과 출력
//		for (int i = 0; i < sortedResultsArray.size(); i++) {
//			GPT_API gpt_api = new GPT_API();
//			System.out.println((i+1)+"번째");
//			JsonObject place = sortedResultsArray.get(i).getAsJsonObject();
//
//			String name = place.get("name").getAsString();
//			JsonObject location = place.getAsJsonObject("location");
//			String address = location.has("address") ? location.get("address").getAsString() : gpt_api.Gpt_Request(name,"address");
//
//			//시도 추출
//			String region = location.has("region") ? location.get("region").getAsString() : "region 정보 없음";
//
//			// 지리 정보 확인
//			if (place.has("geocodes")) {
//				JsonObject geocodes = place.getAsJsonObject("geocodes").getAsJsonObject("main"); // 'main' 키 사용
//				String latitude = String.valueOf(geocodes.has("latitude") ? geocodes.get("latitude").getAsDouble() : 0.0);
//				String longitude = String.valueOf(geocodes.has("longitude") ? geocodes.get("longitude").getAsDouble() : 0.0);
//
//				System.out.println("장소 이름: " + place.get("name").getAsString());
//				System.out.println("위도: " + latitude);
//				System.out.println("경도: " + longitude);
//				System.out.println(); // 추가적인 줄바꿈
//				if (originLat == 0.0){
//					originLat = Double.parseDouble(latitude);
//					originLng = Double.parseDouble(longitude);
//				}else {
//					destinationLat = Double.parseDouble(latitude);
//					destinationLng = Double.parseDouble(longitude);
//				}
//
//			} else {
//				System.out.println("geocodes 정보가 없습니다.");
//			}
//
//
//
//			String regularHours = "영업 시간 없음";
//			if (place.has("hours")) {
//				JsonObject hours = place.getAsJsonObject("hours");
//				if (hours.has("regular")) {
//					JsonArray regularArray = hours.getAsJsonArray("regular");
//					StringBuilder hoursBuilder = new StringBuilder();
//					for (int j = 0; j < regularArray.size(); j++) {
//						JsonObject dayInfo = regularArray.get(j).getAsJsonObject();
//						int day = dayInfo.get("day").getAsInt();
//						String openTime = dayInfo.get("open").getAsString();
//						String closeTime = dayInfo.get("close").getAsString();
//						hoursBuilder.append("요일: ").append(getDayName(day))
//								.append(", 오픈: ").append(openTime)
//								.append(", 마감: ").append(closeTime).append("\n");
//					}
//					regularHours = hoursBuilder.toString();
//				}
//
//				if(regularHours.equals("영업 시간 없음")){
//					String string_rh = gpt_api.Gpt_Request(name,"regularHours");
//					String string_rh_GPT = regularHours_GPT(string_rh);
//					regularHours = string_rh_GPT;
//				}
//			}
//
//
//			String priceTier = place.has("price") ? place.get("price").getAsString() : gpt_api.Gpt_Request(name,"priceTier");
//			String rating = place.has("rating") ? place.get("rating").getAsString() : "평점 없음";
//
//			String statsInfo = "리뷰 및 평점 수 정보 없음";
//			if (place.has("stats")) {
//				JsonObject stats = place.getAsJsonObject("stats");
//				int ratingsCount = stats.has("total_ratings") ? stats.get("total_ratings").getAsInt() : 0;
//				int tips = stats.has("total_tips") ? stats.get("total_tips").getAsInt() : 0;
//				statsInfo = "평점 수: " + ratingsCount + ", 리뷰/팁 수: " + tips;
//			}
//
//			String categoriesInfo = "카테고리 정보 없음";
//			if (place.has("categories")) {
//				JsonArray categoriesArray = place.getAsJsonArray("categories");
//				StringBuilder categoriesBuilder = new StringBuilder();
//				for (int j = 0; j < categoriesArray.size(); j++) {
//					JsonObject category = categoriesArray.get(j).getAsJsonObject();
//					String categoryName = category.get("name").getAsString();
//					String categoryId = category.get("id").getAsString();
//					categoriesBuilder.append("ID: ").append(categoryId).append(", 이름: ").append(categoryName);
//					if (j < categoriesArray.size() - 1) {
//						categoriesBuilder.append("; ");
//					}
//				}
//				categoriesInfo = categoriesBuilder.toString();
//			}
//
//			// 출력
//			System.out.println("관광명소 이름: " + name);
//			System.out.println("주소: " + address);
//			System.out.println("formatted_address : " + region);
////			System.out.println("위도,경도: " + geocodeBuilder.toString());
//			System.out.println("영업 시간:\n" + regularHours);
//			System.out.println("가격대: " + priceTier);
//			System.out.println("평점: " + rating);
//			System.out.println("인기도 세부 사항: " + statsInfo);
////			System.out.println("태그: " + tags);
//			System.out.println("카테고리: " + categoriesInfo);
////			System.out.println("평균 관람 시간");
//
////			gpt_time(name); //평균 관람 시간 구하기
//			System.out.println("-------------------------");
////			String string_rh = gpt_api.Gpt_Request(name,"regularHours");
////			String ss = regularHours_GPT(string_rh);
////			System.out.println(ss);
//		}


		////========================Foursquare API 호출 stats 요소 확인용=================================================
//		OkHttpClient client = new OkHttpClient();
//
//		// Foursquare Place Search API 요청
//		Request request = new Request.Builder()
//				.url("https://api.foursquare.com/v3/places/search?query=서울&categories=16000&limit=5&fields=name,location,hours,price,popularity,rating,stats,tastes")
//				.get()
//				.addHeader("Accept", "application/json")
//				.addHeader("Authorization", "fsq3uJb5W14pqg1wFsyGryVzpaJeut5pRRfy0PnZunTmhRI=")  // 발급받은 API 키 사용
//				.build();
//
//		// API 응답 받기
//		Response response = client.newCall(request).execute();
//		String jsonData = response.body().string();
//
//		// 응답 데이터를 JSON으로 파싱
//		JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
//		JsonArray resultsArray = jsonObject.getAsJsonArray("results");
//
//		// 관광명소 정보 출력
//		for (int i = 0; i < resultsArray.size(); i++) {
//			JsonObject place = resultsArray.get(i).getAsJsonObject();
//
//			// 장소 이름 추출
//			String name = place.get("name").getAsString();
//			System.out.println("장소 이름: " + name);
////			String price = place.get("price").getAsString();
////			System.out.println("가격 : " + price);
//
//			// stats 필드 확인
//			if (place.has("stats")) {
//				JsonObject stats = place.getAsJsonObject("stats");
//
//				// total_photos (사진 수)
//				String totalPhotos = stats.has("total_photos") ? stats.get("total_photos").getAsString() : "정보 없음";
//				System.out.println("사진 수: " + totalPhotos);
//
//				// total_ratings (평점 수)
//				String totalRatings = stats.has("total_ratings") ? stats.get("total_ratings").getAsString() : "정보 없음";
//				System.out.println("평점 수: " + totalRatings);
//
//				// total_tips (리뷰/팁 수)
//				String totalTips = stats.has("total_tips") ? stats.get("total_tips").getAsString() : "정보 없음";
//				System.out.println("리뷰/팁 수: " + totalTips);
//
//				// total_likes (좋아요 수) - 선택적으로 포함될 수 있음
//				String totalLikes = stats.has("total_likes") ? stats.get("total_likes").getAsString() : "정보 없음";
//				System.out.println("좋아요 수: " + totalLikes);
//
//				// checkins_count (체크인 수) - 선택적으로 포함될 수 있음
//				String checkinsCount = stats.has("checkins_count") ? stats.get("checkins_count").getAsString() : "정보 없음";
//				System.out.println("체크인 수: " + checkinsCount);
//			} else {
//				System.out.println("통계 정보 없음");
//			}
//
//			System.out.println("-------------------------");
//		}


////========================Foursquare API 호출 리뷰 출력 확인용=================================================
//		OkHttpClient client = new OkHttpClient();
//
//		// Foursquare Place Search API 요청 (경복궁 근처 음식점 검색)
//		Request request = new Request.Builder()
//				.url("https://api.foursquare.com/v3/places/search?categories=16020,16031&limit=20&near=서울&fields=name,location,categories,hours,price,popularity,rating,stats,tastes")
//				.get()
//				.addHeader("Accept", "application/json")
//				.addHeader("Authorization", "fsq3uJb5W14pqg1wFsyGryVzpaJeut5pRRfy0PnZunTmhRI=")  // 발급받은 API 키 사용
//				.build();
//
//		// API 응답 받기
//		Response response = client.newCall(request).execute();
//		String jsonData = response.body().string();
//
//		// 응답 데이터를 JSON으로 파싱
//		JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
//		JsonArray resultsArray = jsonObject.getAsJsonArray("results");
//
//		// 음식점 정보 출력 (최대 5개)
//		for (int i = 0; i < resultsArray.size(); i++) {
//			System.out.println((i+1)+"번째");
//			JsonObject place = resultsArray.get(i).getAsJsonObject();
//
//			// 장소 이름과 위치 정보 추출
//			String name = place.get("name").getAsString();
//			JsonObject location = place.getAsJsonObject("location");
//			String address = location.has("address") ? location.get("address").getAsString() : "주소 없음";
//			if (place.has("stats")) {
//				JsonObject stats = place.getAsJsonObject("stats");
//
//				// total_photos (사진 수)
//				String totalPhotos = stats.has("total_photos") ? stats.get("total_photos").getAsString() : "정보 없음";
//				System.out.println("사진 수: " + totalPhotos);
//
//				// total_ratings (평점 수)
//				String totalRatings = stats.has("total_ratings") ? stats.get("total_ratings").getAsString() : "정보 없음";
//				System.out.println("평점 수: " + totalRatings);
//
//				// total_tips (리뷰/팁 수)
//				String totalTips = stats.has("total_tips") ? stats.get("total_tips").getAsString() : "정보 없음";
//				System.out.println("리뷰/팁 수: " + totalTips);
//
//				// total_likes (좋아요 수) - 선택적으로 포함될 수 있음
//				String totalLikes = stats.has("total_likes") ? stats.get("total_likes").getAsString() : "정보 없음";
//				System.out.println("좋아요 수: " + totalLikes);
//
//				// checkins_count (체크인 수) - 선택적으로 포함될 수 있음
//				String checkinsCount = stats.has("checkins_count") ? stats.get("checkins_count").getAsString() : "정보 없음";
//				System.out.println("체크인 수: " + checkinsCount);
//			} else {
//				System.out.println("통계 정보 없음");
//			}
//			// 리뷰/팁 정보 추출
//			String tips = "리뷰 정보 없음";
//			if (place.has("tips")) {
//				JsonArray tipsArray = place.getAsJsonArray("tips");
//				StringBuilder tipsBuilder = new StringBuilder();
//				for (int j = 0; j < tipsArray.size(); j++) {
//					JsonObject tip = tipsArray.get(j).getAsJsonObject();
//					String text = tip.get("text").getAsString();  // 리뷰/팁 내용
//					tipsBuilder.append((j + 1) + ". ").append(text).append("\n");
//				}
//				tips = tipsBuilder.toString();
//			}
//						String categoriesInfo = "카테고리 정보 없음";
//			if (place.has("categories")) {
//				JsonArray categoriesArray = place.getAsJsonArray("categories");
//				StringBuilder categoriesBuilder = new StringBuilder();
//				for (int j = 0; j < categoriesArray.size(); j++) {
//					JsonObject category = categoriesArray.get(j).getAsJsonObject();
//					String categoryName = category.get("name").getAsString();
//					String categoryId = category.get("id").getAsString();
//					categoriesBuilder.append("ID: ").append(categoryId).append(", 이름: ").append(categoryName);
//					if (j < categoriesArray.size() - 1) {
//						categoriesBuilder.append("; ");
//					}
//				}
//				categoriesInfo = categoriesBuilder.toString();
//			}
//
//			// 출력
//			System.out.println("음식점 이름: " + name);
//			System.out.println("주소: " + address);
//			System.out.println("리뷰/팁:\n" + tips);
//			System.out.println("카테고리: " + categoriesInfo);
//			System.out.println("-------------------------");
//		}

////========================GPT API 호출=================================================
//		String model = "gpt-4o-mini"; // 모델을 gpt-4o-mini로 수정
//		String prompt = "경복궁 평균 관람 시간을 분단위로 알려줘";
//
//
//		try {
//			String url = "https://api.openai.com/v1/chat/completions";
//			JSONObject requestBody = new JSONObject();
//			requestBody.put("model", model);
//			requestBody.put("max_tokens", MAX_TOKENS);
//			JSONArray messages = new JSONArray();
//			JSONObject message = new JSONObject();
//			message.put("role", "user");
//			message.put("content", prompt);
//			messages.put(message);
//			requestBody.put("messages", messages);
//
////			// "tools" 배열 생성
////			JSONArray tools = new JSONArray();
////			JSONObject tool = new JSONObject();
////			tool.put("type", "function");
////
////			JSONObject function = new JSONObject();
////			function.put("name", "myFunction");
////			function.put("description", "함수의 동작 정의");
////
////			// 파라미터 정의
////			JSONObject parameters = new JSONObject();
////			parameters.put("type", "object");
////
////			JSONObject properties = new JSONObject();
////			properties.put("averageVisitTime", new JSONObject().put("type", "number"));
////
////			parameters.put("properties", properties);
////			parameters.put("required", new JSONArray().put("averageVisitTime"));
////
////			function.put("parameters", parameters);
////			tool.put("function", function);
////			tools.put(tool);
////
////			// "tools" 배열을 요청 본문에 추가
////			requestBody.put("tools", tools);
////
////			// "tool_choice" 설정
////			JSONObject toolChoice = new JSONObject();
////			toolChoice.put("type", "function");
////
////			JSONObject toolFunction = new JSONObject();
////			toolFunction.put("name", "myFunction"); // arguments를 삭제하였습니다.
////
////			toolChoice.put("function", toolFunction);
////			requestBody.put("tool_choice", toolChoice);
//
//			// temperature 설정
//			requestBody.put("temperature", 0.4); // 온도 설정
//
//			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//			connection.setRequestMethod("POST");
//			connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
//			connection.setRequestProperty("Content-Type", "application/json");
//			connection.setDoOutput(true);
//
//			try (OutputStream os = connection.getOutputStream()) {
//				byte[] input = requestBody.toString().getBytes("utf-8");
//				os.write(input, 0, input.length);
//			}
//
//			StringBuilder response = new StringBuilder();
//			try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
//				String responseLine;
//				while ((responseLine = br.readLine()) != null) {
//					response.append(responseLine.trim());
//				}
//			}
//
//			parseAndPrintResponse(response.toString());
//
//		} catch (Exception e) {
//			System.err.println("API 호출 중 오류 발생: " + e.getMessage());
//		}



////========================숙소를 받아 오기 위한구글 플레이스 api 호출================================================
//		String LOCATION = "37.5665,126.9780";
//		int RADIUS = 5000;
//		double LATITUDE = 37.5665;
//		double LONGITUDE = 126.9780;
//
//		try {
//			// 요청 URL 생성 (type을 lodging으로 설정)
//			String urlString = String.format(
//					"https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s&radius=%d&type=lodging&language=ko&rankby=prominence&key=%s",
//					LOCATION, RADIUS, GooGleMap_API_KEY
//			);
//
//			// URL 객체 생성 및 연결 설정
//			URL url = new URL(urlString);
//			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//			connection.setRequestMethod("GET");
//
//			// 응답 코드 확인
//			int responseCode = connection.getResponseCode();
//			if (responseCode == HttpURLConnection.HTTP_OK) {
//				// 응답 데이터 읽기
//				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//				String inputLine;
//				StringBuilder response = new StringBuilder();
//
//				while ((inputLine = in.readLine()) != null) {
//					response.append(inputLine);
//				}
//				in.close();
//
//				// JSON 파싱
//				parseAndPrintPlaces(response.toString());
//			} else {
//				System.out.println("API 요청 실패: " + responseCode);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}
	private static String regularHours_GPT(String gt) throws JSONException {
		// JSON 문자열
		String jsonString = """
        {
            "hours": {
                %s
            }
        }""".formatted(gt);;

		// 기본 값 설정
		String regularHours = "영업 시간 없음";
		JsonObject place = JsonParser.parseString(jsonString).getAsJsonObject();

		// "hours" 객체가 있는지 확인
		if (place.has("hours")) {
			JsonObject hours = place.getAsJsonObject("hours");
			// "regular" 배열이 있는지 확인
			if (hours.has("regular")) {
				JsonArray regularArray = hours.getAsJsonArray("regular");
				StringBuilder hoursBuilder = new StringBuilder();
				for (int j = 0; j < regularArray.size(); j++) {
					JsonObject dayInfo = regularArray.get(j).getAsJsonObject();
					int day = dayInfo.get("day").getAsInt();
					String openTime = dayInfo.get("open").getAsString();
					String closeTime = dayInfo.get("close").getAsString();
					hoursBuilder.append("요일: ").append(getDayName(day))
							.append(", 오픈: ").append(openTime)
							.append(", 마감: ").append(closeTime).append("\n");
				}
				// StringBuilder의 내용을 regularHours에 저장
				regularHours = hoursBuilder.toString().trim(); // 마지막 줄바꿈 제거
			}
		}


//		System.out.println(regularHours);
		return regularHours;
	}


	//구글 플레이스 api로 JSON 데이터 파싱 및 출력
	private static void parseAndPrintPlaces(String jsonResponse) throws JSONException {
		JSONObject jsonObject = new JSONObject(jsonResponse);
		JSONArray results = jsonObject.getJSONArray("results");

		// JSONArray를 List<JSONObject>로 변환
		List<JSONObject> placesList = new ArrayList<>();
		for (int i = 0; i < results.length(); i++) {
			placesList.add(results.getJSONObject(i));
		}

		// 리뷰 수 기준으로 내림차순 정렬
		placesList.sort((place1, place2) -> {
			int reviews1 = place1.optInt("user_ratings_total", 0);
			int reviews2 = place2.optInt("user_ratings_total", 0);
			return Integer.compare(reviews2, reviews1); // 내림차순 정렬
		});

		// 정렬된 결과 출력
		for (JSONObject place : placesList) {

			// 명소 이름
			String name = place.getString("name");

			// 위치 (위도, 경도)
			double lat = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
			double lng = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

			// 평점 (rating 값이 없으면 -1)
			double rating = place.has("rating") ? place.getDouble("rating") : -1;

			// 리뷰 수 (user_ratings_total 값이 없으면 -1)
			int userRatingsTotal = place.has("user_ratings_total") ? place.getInt("user_ratings_total") : -1;
			
			// 주소 (formatted_address가 있을 경우)
			String address = place.has("vicinity") ? place.getString("vicinity") : "주소 정보 없음";

			// 정보 출력
			System.out.println("숙소 이름: " + name);
//			gpt_HotelStar(name,address);
			System.out.println("위치: " + lat + ", " + lng);
			System.out.println("주소: " + address);
			System.out.println("평점: " + rating);
			System.out.println("리뷰 수: " + userRatingsTotal);
			System.out.println("------------------------");
		}
	}
	
	// 평균 관람 시간 받아옴
	private  static void gpt_time(String place){
		////========================GPT API function 호출=================================================
		String url = "https://api.openai.com/v1/chat/completions";
//		String place = "경복궁";
		String authorizationKey = "Bearer "+API_KEY; // 여기에 자신의 API 키 입력
		String jsonInputString = "{"
				+ "\"model\": \"gpt-4o-mini\","
				+ "\"messages\": ["
				+ "  {"
				+ "    \"role\": \"user\","
				+ "    \"content\": \"" + place + " 평균 관람 시간을 분단위로 알려줘\""
				+ "  }"
				+ "],"
				+ "\"tools\": ["
				+ "  {"
				+ "    \"type\": \"function\","
				+ "    \"function\": {"
				+ "      \"name\": \"myFunction\","
				+ "      \"description\": \"함수의 동작 정의\","
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
					int averageVisitTime = argumentsJson.getInt("averageVisitTime");
					System.out.println("평균 관람 시간: " + averageVisitTime+"분");
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
	}

	// 관광지 추천 이유 받아옴
	private static String gpt_Reason(String place){
		////========================GPT API function 호출=================================================
		String url = "https://api.openai.com/v1/chat/completions";
		String authorizationKey = "Bearer "+API_KEY; // 여기에 자신의 API 키 입력
		String jsonInputString = "{"
				+ "\"model\": \"gpt-4o-mini\","
				+ "\"messages\": ["
				+ "  {"
				+ "    \"role\": \"user\","
				+ "    \"content\": \"" + place + "의 여행지 추천 이유를 70글자 이내로 알려줘\""
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
		String answer = "";

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

				System.out.println(argumentsString);


				JSONObject argumentsJson = new JSONObject(argumentsString);

				// averageVisitTime 값 추출
				if (argumentsJson.has("answer")) {
					answer = argumentsJson.getString("answer");
					System.out.println(answer);

				} else {
					System.out.println("No value for 'answer'");
				}
			} else {
				System.out.println("No 'tool_calls' found in the response.");
			}


			// 사용된 토큰 정보 확인
			if (jsonResponse.has("usage")) {
				JSONObject usage = jsonResponse.getJSONObject("usage");
				int promptTokens = usage.getInt("prompt_tokens");
				int completionTokens = usage.getInt("completion_tokens");
				int totalTokens = usage.getInt("total_tokens");

				// 토큰 정보 출력
				System.out.println("Prompt Tokens: " + promptTokens);
				System.out.println("Completion Tokens: " + completionTokens);
				System.out.println("Total Tokens: " + totalTokens);
			} else {
				System.out.println("No usage information found.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		return answer;
	}

	// 숙소가 몇 성인지 받아옴
	private  static void gpt_HotelStar(String place){
		////========================GPT API function 호출=================================================
		String url = "https://api.openai.com/v1/chat/completions";
		String authorizationKey = "Bearer "+API_KEY; // 여기에 자신의 API 키 입력
		String jsonInputString = "{"
				+ "\"model\": \"gpt-4o-mini\","
				+ "\"messages\": ["
				+ "  {"
				+ "    \"role\": \"user\","
				+ "    \"content\": \""+ place + "의 성급 정보을 부킹 닷컴에서 찾아서 알려줘. 정보가 없다면 트립어드바이저에서 유용한 정보 칸에 있는 호텔 등급 값을 찾아서 알려줘\""
				+ "  }"
				+ "],"
				+ "\"tools\": ["
				+ "  {"
				+ "    \"type\": \"function\","
				+ "    \"function\": {"
				+ "      \"name\": \"myFunction\","
				+ "      \"description\": \"함수의 동작 정의\","
				+ "      \"parameters\": {"
				+ "        \"type\": \"object\","
				+ "        \"properties\": {"
				+ "          \"StarRank\": {"
				+ "            \"type\": \"number\""
				+ "          }"
				+ "        },"
				+ "        \"required\": [\"StarRank\"]"
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
				if (argumentsJson.has("StarRank")) {
					int StarRank = argumentsJson.getInt("StarRank");
					System.out.println(place+" : "+StarRank+"성급");
				} else {
					System.out.println("정보 없음");
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


	// 요일 숫자를 요일 이름으로 변환하는 메서드
	private static String getDayName(int day) {
		switch (day) {
			case 1: return "월요일";
			case 2: return "화요일";
			case 3: return "수요일";
			case 4: return "목요일";
			case 5: return "금요일";
			case 6: return "토요일";
			case 7: return "일요일";
			default: return "알 수 없음";
		}
	}


	
//	private static void parseAndPrintResponse(String responseBody) throws JSONException {
//		JSONObject jsonObject = new JSONObject(responseBody);
//		JSONArray choices = jsonObject.getJSONArray("choices");
//
//		for (int i = 0; i < choices.length(); i++) {
//			String text = choices.getJSONObject(i).getJSONObject("message").getString("content");
//			printInChunks(text, BUFFER_SIZE);
//
//			// Check for continuation token and fetch next response if available
//			if (jsonObject.has("next")) {
//				fetchNextResponse(jsonObject.getString("next"));
//			}
//		}
//	}
//	private static void fetchNextResponse(String nextToken) {
//		// Implementation for fetching next response using the continuation token
//		try {
//			// API URL 설정
//			String url = "https://api.openai.com/v1/chat/completions";
//
//			// JSON 요청 본문 생성
//			JSONObject requestBody = new JSONObject();
//			requestBody.put("next", nextToken);
//
//			// HTTP 연결 설정
//			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//			connection.setRequestMethod("POST");
//			connection.setRequestProperty("Authorization", "Bearer " + GooGleMap_API_KEY);
//			connection.setRequestProperty("Content-Type", "application/json");
//			connection.setDoOutput(true);
//
//			// 요청 본문 전송
//			try (OutputStream os = connection.getOutputStream()) {
//				byte[] input = requestBody.toString().getBytes("utf-8");
//				os.write(input, 0, input.length);
//			}
//
//			// 응답 받기
//			StringBuilder response = new StringBuilder();
//			try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
//				String responseLine;
//				while ((responseLine = br.readLine()) != null) {
//					response.append(responseLine.trim());
//				}
//			}
//
//			// 응답 파싱 및 출력
//			parseAndPrintResponse(response.toString());
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static void printInChunks(String text, int bufferSize) {
//		int length = text.length();
//		int start = 0;
//
//		while (start < length) {
//			int end = Math.min(length, start + bufferSize);
//			System.out.println(text.substring(start, end));
//			start = end;
//		}
//	}

}