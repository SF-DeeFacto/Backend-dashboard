package com.backend_dashboard.backend_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableCaching
@SpringBootApplication
public class BackendDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendDashboardApplication.class, args);
	}

}
