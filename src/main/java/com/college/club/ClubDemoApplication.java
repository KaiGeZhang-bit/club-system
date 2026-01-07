package com.college.club;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.college.club.mapper")
public class  ClubDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClubDemoApplication.class, args);
    }

}
