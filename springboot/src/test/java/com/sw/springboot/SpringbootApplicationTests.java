package com.sw.springboot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;


import java.io.IOException;
import okhttp3.*;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@SpringBootTest
class SpringbootApplicationTests {

	@Test
	void contextLoads() throws IOException, InterruptedException {
//		OkHttpClient client = new OkHttpClient();
//
//		Request request = new Request.Builder()
//				.url("https://api.foursquare.com/v3/places/search?query=%EA%B2%BD%EB%B3%B5%EA%B6%81")
//				.get()
//				.addHeader("accept", "application/json")
//				.addHeader("Authorization", "fsq3mCnSlowyrkgm+8j9lnpOboxjJbyoYOG3nxai/xUnays=")
//				.build();
//
//		Response response = client.newCall(request).execute();


//		HttpRequest request = HttpRequest.newBuilder()
//				.uri(URI.create("https://api.foursquare.com/v3/places/search?query=센트럴파크"))
//				.header("accept", "application/json")
//				.header("Authorization", "fsq3mCnSlowyrkgm+8j9lnpOboxjJbyoYOG3nxai/xUnays=")
//				.method("GET", HttpRequest.BodyPublishers.noBody())
//				.build();
//		HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
//		System.out.println(response.body());

//		  .url("https://api.foursquare.com/v3/places/search?query=Burgers" +
//				"&ll=41.8781,-87.6298" +
//				"&radius=36105&categories=13000&" +
//				"exclude_all_chains=true&fields=fsq_id,name,location" +
//				"&min_price=3&" +
//				"max_price=3&open_at=&open_now=true&near=&sort=DISTANCE&limit=20&" +
//				"session_token={{session_token}}")
//				.method("GET", body)
//				.addHeader("Accept", "application/json")
//				.build();


//
//		OkHttpClient client = new OkHttpClient();
//
//		Request request = new Request.Builder()
//				.url("https://api.foursquare.com/v3/places/search?ll=37.5796,126.9770&radius=1000&categories=13000&limit=10&fields=fsq_id,name,location,hours,popularity,rating")
//				.get()
//				.addHeader("Accept", "application/json")
//				.addHeader("Authorization", "fsq3uJb5W14pqg1wFsyGryVzpaJeut5pRRfy0PnZunTmhRI=")  // 발급받은 API 키 사용
//				.build();
//
//		Response response = client.newCall(request).execute();
//		String jsonData = response.body().string();
//
//		// Gson을 사용하여 JSON 데이터를 보기 좋게 출력
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		String prettyJson = gson.toJson(gson.fromJson(jsonData, Object.class));
//
//		// 포맷된 JSON 출력
//		System.out.println(prettyJson);
		OkHttpClient client = new OkHttpClient();

// Foursquare Place Search API 요청 (인기도 순 정렬 및 카테고리 ID 포함)
		Request request = new Request.Builder()
				.url("https://api.foursquare.com/v3/places/search?ll=37.5665,126.9780&radius=10000&categories=16011,16020,16025,16026,16031,17020,17033,17030,17036,17039,17089,17104,17105,17114,17115&limit=50&sort=RATING&fields=name,location,hours,price,popularity,rating,stats,tastes,categories")
				.get()
				.addHeader("Accept", "application/json")
				.addHeader("Authorization", "fsq3uJb5W14pqg1wFsyGryVzpaJeut5pRRfy0PnZunTmhRI=")  // 발급받은 API 키 사용
				.build();

		// API 응답 받기
		Response response = client.newCall(request).execute();
		String jsonData = response.body().string();

		// 응답 데이터를 JSON으로 파싱
		JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
		JsonArray resultsArray = jsonObject.getAsJsonArray("results");
// JsonArray -> List<JsonObject> 변환
		List<JsonObject> placesList = new ArrayList<>();
		for (int i = 0; i < resultsArray.size(); i++) {
			placesList.add(resultsArray.get(i).getAsJsonObject());
		}

// 리뷰 수와 좋아요 수를 합산한 값으로 List 정렬
		placesList.sort((place1, place2) -> {
			JsonObject stats1 = place1.getAsJsonObject("stats");
			JsonObject stats2 = place2.getAsJsonObject("stats");

			// 리뷰 수와 좋아요 수 가져오기 (값이 없을 경우 0으로 처리)
			int reviewCount1 = stats1.has("total_ratings") ? stats1.get("total_ratings").getAsInt() : 0;
			int likes1 = stats1.has("total_likes") ? stats1.get("total_likes").getAsInt() : 0;
			int reviewCount2 = stats2.has("total_ratings") ? stats2.get("total_ratings").getAsInt() : 0;
			int likes2 = stats2.has("total_likes") ? stats2.get("total_likes").getAsInt() : 0;

			// 리뷰 수와 좋아요 수의 합 계산
			int total1 = reviewCount1 + likes1;
			int total2 = reviewCount2 + likes2;

			// 합이 높은 순으로 정렬 (내림차순)
			return Integer.compare(total2, total1);
		});

// 다시 List<JsonObject> -> JsonArray 변환
		JsonArray sortedResultsArray = new JsonArray();
		for (JsonObject place : placesList) {
			sortedResultsArray.add(place);
		}

// 정렬된 결과 출력
		for (int i = 0; i < sortedResultsArray.size(); i++) {
			JsonObject place = sortedResultsArray.get(i).getAsJsonObject();
			// 기존 출력 로직 유지
			String name = place.get("name").getAsString();
			JsonObject location = place.getAsJsonObject("location");
			String address = location.has("address") ? location.get("address").getAsString() : "주소 없음";
			String regularHours = "영업 시간 없음";
			if (place.has("hours")) {
				JsonObject hours = place.getAsJsonObject("hours");
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
					regularHours = hoursBuilder.toString();
				}
			}

			String priceTier = place.has("price") ? place.get("price").getAsString() : "가격대 정보 없음";
			String rating = place.has("rating") ? place.get("rating").getAsString() : "평점 없음";
			String popularity = place.has("popularity") ? place.get("popularity").getAsString() : "인기도 정보 없음";

			String statsInfo = "리뷰 및 좋아요 정보 없음";
			if (place.has("stats")) {
				JsonObject stats = place.getAsJsonObject("stats");
				int reviewCount = stats.has("total_ratings") ? stats.get("total_ratings").getAsInt() : 0;
				int likes = stats.has("total_likes") ? stats.get("total_likes").getAsInt() : 0;
				statsInfo = "리뷰 수: " + reviewCount + ", 좋아요 수: " + likes;
			}

			String tags = "태그 정보 없음";
			if (place.has("tastes")) {
				JsonArray tastesArray = place.getAsJsonArray("tastes");
				StringBuilder tagsBuilder = new StringBuilder();
				for (int j = 0; j < tastesArray.size(); j++) {
					tagsBuilder.append(tastesArray.get(j).getAsString());
					if (j < tastesArray.size() - 1) {
						tagsBuilder.append(", ");
					}
				}
				tags = tagsBuilder.toString();
			}

			String categoriesInfo = "카테고리 정보 없음";
			if (place.has("categories")) {
				JsonArray categoriesArray = place.getAsJsonArray("categories");
				StringBuilder categoriesBuilder = new StringBuilder();
				for (int j = 0; j < categoriesArray.size(); j++) {
					JsonObject category = categoriesArray.get(j).getAsJsonObject();
					String categoryName = category.get("name").getAsString();
					String categoryId = category.get("id").getAsString();
					categoriesBuilder.append("ID: ").append(categoryId).append(", 이름: ").append(categoryName);
					if (j < categoriesArray.size() - 1) {
						categoriesBuilder.append("; ");
					}
				}
				categoriesInfo = categoriesBuilder.toString();
			}

			// 출력
			System.out.println("관광명소 이름: " + name);
			System.out.println("주소: " + address);
			System.out.println("영업 시간:\n" + regularHours);
			System.out.println("가격대: " + priceTier);
			System.out.println("평점: " + rating);
			System.out.println("인기도 점수: " + popularity);
			System.out.println("인기도 세부 사항: " + statsInfo);
			System.out.println("태그: " + tags);
			System.out.println("카테고리: " + categoriesInfo);
			System.out.println("-------------------------");
		}
	}
	// 요일 숫자를 요일 이름으로 변환하는 메서드
	private static String getDayName(int day) {
		switch (day) {
			case 0: return "일요일";
			case 1: return "월요일";
			case 2: return "화요일";
			case 3: return "수요일";
			case 4: return "목요일";
			case 5: return "금요일";
			case 6: return "토요일";
			default: return "알 수 없음";
		}
	}
}