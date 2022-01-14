package com.vadmack.mongodbtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@PropertySources({
        @PropertySource("classpath:application-env.properties"),
        @PropertySource(value = "classpath:application-env.local.properties", ignoreResourceNotFound = true)
})
public class MongoDbTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongoDbTestApplication.class, args);
    }

}
