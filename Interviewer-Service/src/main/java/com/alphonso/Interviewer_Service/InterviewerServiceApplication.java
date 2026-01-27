package com.alphonso.Interviewer_Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.alphonso.Interviewer_Service.FeignClient"})
@EnableDiscoveryClient
@EnableScheduling
public class InterviewerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewerServiceApplication.class, args);
	}

}
