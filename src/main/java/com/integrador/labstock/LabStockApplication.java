package com.integrador.labstock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LabStockApplication {

	public static void main(String[] args) {
		SpringApplication.run(LabStockApplication.class, args);
	}

}