package com.libraryManagement.project;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		String password = dotenv.get("DB_PASSWORD");
		String url = dotenv.get("DB_URL");
		String username = dotenv.get("DB_USERNAME");

		System.setProperty("db.password",password);
		System.setProperty("db.username",username);
		System.setProperty("db.url",url);



		SpringApplication.run(ProjectApplication.class, args);
	}

}
