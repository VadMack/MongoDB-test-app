package com.vadmack.mongodbtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class MongoDbTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoDbTestApplication.class, args);
    }

}
