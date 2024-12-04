package com.sw.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public String addUser(@RequestBody User userDto) {
        User user = User.builder()
                .user_name(userDto.getUser_name())
                .country(userDto.getCountry())
                .tel(userDto.getTel())
                .birth(userDto.getBirth())
                .build();

        userRepository.save(user); // INSERT 실행
        return "User added successfully!";
    }
}
