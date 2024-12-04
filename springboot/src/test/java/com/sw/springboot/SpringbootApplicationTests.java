package com.sw.springboot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;


import java.io.IOException;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.*;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

@SpringBootTest
class SpringbootApplicationTests {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	UserController userController;

	@Test
	void contextLoads() throws IOException, InterruptedException, JSONException {

		// User 객체 생성
		User user = User.builder()
				.user_name("John Doe")
				.country("USA")
				.tel("123-456-7890")
				.birth(LocalDate.of(1990, 1, 1))
				.build();


		userController.addUser(user);

		// 데이터베이스에 저장
		userRepository.save(user);


		// 확인용 출력
		System.out.println("User added: " + user);

	}
}