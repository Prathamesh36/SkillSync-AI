package com.codingshuttle.hackathon.skillsyncai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SkillSyncAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillSyncAiApplication.class, args);
	}

}
