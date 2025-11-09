package com.tp.foodai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FoodaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodaiApplication.class, args);
	}

}
