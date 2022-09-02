package com.happy_time.happy_time;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.happy_time.happy_time.*")
public class HappyTimeApplication {

	//hiihii
	public static void main(String[] args) {
		SpringApplication.run(HappyTimeApplication.class, args);
	}
}
