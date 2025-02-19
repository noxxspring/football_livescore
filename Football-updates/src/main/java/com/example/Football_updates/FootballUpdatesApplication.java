package com.example.Football_updates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FootballUpdatesApplication {

	public static void main(String[] args) {
		SpringApplication.run(FootballUpdatesApplication.class, args);
	}

}
