package com.qapel.rfid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.qapel.rfid.controller","com.qapel.rfid",
		"com.qapel.rfid.event","com.qapel.rfid.configure"})
public class RfidApplication {

	public static void main(String[] args) {
		SpringApplication.run(RfidApplication.class, args);
	}
}
