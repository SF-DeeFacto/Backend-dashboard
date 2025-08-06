package com.backend_dashboard.backend_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BackendDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendDashboardApplication.class, args);
	}

}
