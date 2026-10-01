package com.example.test_reactive_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;


@SpringBootApplication
@EnableR2dbcRepositories
public class TestReactiveSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestReactiveSpringApplication.class, args);
	}

}
