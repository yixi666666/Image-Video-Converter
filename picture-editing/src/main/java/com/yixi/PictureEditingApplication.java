package com.yixi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class PictureEditingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureEditingApplication.class, args);
    }
    ;;;;;;;;;;;;;;
}
