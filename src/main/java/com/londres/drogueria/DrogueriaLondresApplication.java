package com.londres.drogueria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class DrogueriaLondresApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrogueriaLondresApplication.class, args);
	}
}
