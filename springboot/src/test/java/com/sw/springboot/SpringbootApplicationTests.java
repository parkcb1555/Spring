package com.sw.springboot;

import okhttp3.*;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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



		OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
				.url("https://api.foursquare.com/v3/places/search?ll=37.5796,126.9770&radius=1000&categories=13000&limit=10&fields=fsq_id,name,location,hours,popularity,rating")
				.get()
				.addHeader("Accept", "application/json")
				.addHeader("Authorization", "fsq3uJb5W14pqg1wFsyGryVzpaJeut5pRRfy0PnZunTmhRI=")  // 발급받은 API 키 사용
				.build();

		Response response = client.newCall(request).execute();
		System.out.println(response.body().string());
	}

}
