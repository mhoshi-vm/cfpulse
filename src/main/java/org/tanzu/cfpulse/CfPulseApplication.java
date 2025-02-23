package org.tanzu.cfpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CfPulseApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfPulseApplication.class, args);
	}

}
