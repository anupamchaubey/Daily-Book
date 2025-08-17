package com.DailyBook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class DailyBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(DailyBookApplication.class, args);
	}

}
