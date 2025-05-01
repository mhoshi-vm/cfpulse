package org.tanzu.cfpulse;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.tanzu.cfpulse.cf.CfService;
import org.tanzu.cfpulse.cf.CfTools;

@SpringBootApplication
@EnableAsync
public class CfPulseApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfPulseApplication.class, args);
	}

	@Bean
	ToolCallbackProvider cfMcpTools(CfTools cfTools) {
		return MethodToolCallbackProvider.builder().toolObjects(cfTools).build();
	}
}
