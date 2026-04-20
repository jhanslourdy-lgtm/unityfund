package com.securityapp.gofundme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.securityapp.gofundme.repositories")
@EntityScan(basePackages = "com.securityapp.gofundme.model")
@SpringBootApplication
public class GoFundMeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoFundMeApplication.class, args);
	}

}
