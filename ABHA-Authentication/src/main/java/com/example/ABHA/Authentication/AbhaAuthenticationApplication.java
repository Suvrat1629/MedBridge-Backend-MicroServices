package com.example.ABHA.Authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AbhaAuthenticationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbhaAuthenticationApplication.class, args);
	}

}
