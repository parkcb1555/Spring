package com.sw.springboot;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private String user_name;
    private String country;
    private String tel;
    private LocalDate birth;
}