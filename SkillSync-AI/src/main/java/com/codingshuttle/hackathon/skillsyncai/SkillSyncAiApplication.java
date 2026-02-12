package com.codingshuttle.hackathon.skillsyncai;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SkillSyncAiApplication {

	public static void main(String[] args) {
		// Load .env file
		try {
			Dotenv dotenv = Dotenv.load();
			dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		} catch (Exception e) {
			// .env file might not exist in all environments (e.g. prod), which is fine
			System.out.println("No .env file found or error loading it: " + e.getMessage());
		}

		SpringApplication.run(SkillSyncAiApplication.class, args);
	}

}
