package com.center.waterorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WaterOrderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WaterOrderApplication.class, args);
    }
}
